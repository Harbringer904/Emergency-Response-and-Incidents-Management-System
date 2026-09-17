package emergency.units;

import emergency.capabilities.FuelPowered;
import emergency.capabilities.PatientCarrier;
import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class EvacuationBus extends GroundResponseUnit implements FuelPowered, PatientCarrier {
    public static final double FUEL_CAPACITY = 200.0;
    public static final double KM_PER_LITRE = 4.0;
    public static final int SEAT_CAPACITY = 40;

    private double fuelLevel;
    private int currentPatients;

    public EvacuationBus(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        this(id, name, maxSpeed, trafficFactor, FUEL_CAPACITY, 0, 0.0, true, null, 0);
    }

    public EvacuationBus(String id, String name, double maxSpeed, double trafficFactor,
                         double fuelLevel, int currentPatients, double totalDistanceTravelled,
                         boolean available, String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor, totalDistanceTravelled, available,
                assignedIncidentId, completedIncidents);
        if (fuelLevel < 0 || fuelLevel > FUEL_CAPACITY) {
            throw new InvalidOperationException("Fuel level out of range.");
        }
        if (currentPatients < 0 || currentPatients > SEAT_CAPACITY) {
            throw new InvalidOperationException("Passenger count out of range.");
        }
        this.fuelLevel = fuelLevel;
        this.currentPatients = currentPatients;
    }

    @Override
    public String getCapability() {
        return "EVACUATION";
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        if (!hasFuelFor(2.0 * incident.getDistanceFromBase())) {
            return false;
        }
        if ("EVACUATION".equals(incident.getRequiredCapability())) {
            int needed = (int) Math.round(incident.getWorkload());
            return SEAT_CAPACITY - currentPatients >= needed;
        }
        return true;
    }

    @Override
    public String resourceFailureReason(Incident incident) {
        if (!hasFuelFor(2.0 * incident.getDistanceFromBase())) {
            return "Insufficient fuel";
        }
        if ("EVACUATION".equals(incident.getRequiredCapability())) {
            return "Insufficient passenger capacity";
        }
        return "Insufficient resources";
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);
        score += 0.5 * currentPatients;
        if (fuelLevel < policy.getLowFuelThreshold() * FUEL_CAPACITY) {
            score += policy.getLowFuelPenalty();
        }
        return score;
    }

    @Override
    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        if ("EVACUATION".equals(incident.getRequiredCapability())) {
            int needed = (int) Math.round(incident.getWorkload());
            boardPatients(needed);
            releasePatients(needed);
        }
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Refuel amount must be positive.");
        }
        fuelLevel = Math.min(FUEL_CAPACITY, fuelLevel + amount);
    }

    @Override
    public double getFuelLevel() {
        return fuelLevel;
    }

    @Override
    public double getFuelCapacity() {
        return FUEL_CAPACITY;
    }

    @Override
    public double consumeFuel(double distance) throws InsufficientResourceException, InvalidOperationException {
        if (distance < 0) {
            throw new InvalidOperationException("Distance cannot be negative.");
        }
        double litres = distance / KM_PER_LITRE;
        if (fuelLevel + 1e-9 < litres) {
            throw new InsufficientResourceException("Insufficient fuel for " + distance + " km.");
        }
        fuelLevel -= litres;
        return litres;
    }

    @Override
    public boolean hasFuelFor(double distance) {
        if (distance < 0) {
            return false;
        }
        return fuelLevel + 1e-9 >= distance / KM_PER_LITRE;
    }

    @Override
    public void boardPatients(int number) throws InvalidOperationException {
        if (number < 0 || currentPatients + number > SEAT_CAPACITY) {
            throw new InvalidOperationException("Cannot board that many evacuees.");
        }
        currentPatients += number;
    }

    @Override
    public void releasePatients(int number) throws InvalidOperationException {
        if (number < 0 || number > currentPatients) {
            throw new InvalidOperationException("Cannot release that many evacuees.");
        }
        currentPatients -= number;
    }

    @Override
    public int getPatientCapacity() {
        return SEAT_CAPACITY;
    }

    @Override
    public int getCurrentPatients() {
        return currentPatients;
    }

    @Override
    public String getTypeName() {
        return "EVAC_BUS";
    }

    @Override
    public String extraCsvFields() {
        return "," + getTrafficFactor() + "," + fuelLevel + "," + currentPatients;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Fuel        : " + fuelLevel + " / " + FUEL_CAPACITY + " L");
        System.out.println("  Passengers  : " + currentPatients + " / " + SEAT_CAPACITY);
    }
}
