package emergency.units;

import emergency.capabilities.FuelPowered;
import emergency.capabilities.PatientCarrier;
import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class Ambulance extends GroundResponseUnit implements FuelPowered, PatientCarrier {
    public static final double FUEL_CAPACITY = 80.0;
    public static final double KM_PER_LITRE = 12.0;
    public static final int PATIENT_CAPACITY = 4;

    private double fuelLevel;
    private int currentPatients;

    public Ambulance(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        this(id, name, maxSpeed, trafficFactor, FUEL_CAPACITY, 0, 0.0, true, null, 0);
    }

    public Ambulance(String id, String name, double maxSpeed, double trafficFactor,
                     double fuelLevel, int currentPatients, double totalDistanceTravelled,
                     boolean available, String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor, totalDistanceTravelled, available,
                assignedIncidentId, completedIncidents);
        validateFuel(fuelLevel);
        validatePatients(currentPatients);
        this.fuelLevel = fuelLevel;
        this.currentPatients = currentPatients;
    }

    private static void validateFuel(double fuelLevel) throws InvalidOperationException {
        if (fuelLevel < 0 || fuelLevel > FUEL_CAPACITY) {
            throw new InvalidOperationException("Fuel level must be between 0 and " + FUEL_CAPACITY + ".");
        }
    }

    private static void validatePatients(int currentPatients) throws InvalidOperationException {
        if (currentPatients < 0 || currentPatients > PATIENT_CAPACITY) {
            throw new InvalidOperationException("Patient count must be between 0 and " + PATIENT_CAPACITY + ".");
        }
    }

    @Override
    public String getCapability() {
        return "MEDICAL";
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return false;
        }
        if ("MEDICAL".equals(incident.getRequiredCapability())) {
            int needed = (int) Math.round(incident.getWorkload());
            return remainingSeats() >= needed;
        }
        return true;
    }

    @Override
    public String resourceFailureReason(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return "Insufficient fuel";
        }
        if ("MEDICAL".equals(incident.getRequiredCapability())) {
            return "Insufficient patient capacity";
        }
        return "Insufficient resources";
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);
        score += 3.0 * currentPatients;
        if (fuelLevel < policy.getLowFuelThreshold() * FUEL_CAPACITY) {
            score += policy.getLowFuelPenalty();
        }
        return score;
    }

    @Override
    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        if ("MEDICAL".equals(incident.getRequiredCapability())) {
            int needed = (int) Math.round(incident.getWorkload());
            boardPatients(needed);
            releasePatients(needed);
        }
    }

    private int remainingSeats() {
        return PATIENT_CAPACITY - currentPatients;
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
        if (number < 0) {
            throw new InvalidOperationException("Cannot board a negative number of patients.");
        }
        if (currentPatients + number > PATIENT_CAPACITY) {
            throw new InvalidOperationException("Boarding would exceed patient capacity.");
        }
        currentPatients += number;
    }

    @Override
    public void releasePatients(int number) throws InvalidOperationException {
        if (number < 0) {
            throw new InvalidOperationException("Cannot release a negative number of patients.");
        }
        if (number > currentPatients) {
            throw new InvalidOperationException("Cannot release more patients than currently onboard.");
        }
        currentPatients -= number;
    }

    @Override
    public int getPatientCapacity() {
        return PATIENT_CAPACITY;
    }

    @Override
    public int getCurrentPatients() {
        return currentPatients;
    }

    @Override
    public String getTypeName() {
        return "AMBULANCE";
    }

    @Override
    public String extraCsvFields() {
        return "," + getTrafficFactor() + "," + fuelLevel + "," + currentPatients;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Fuel        : " + fuelLevel + " / " + FUEL_CAPACITY + " L");
        System.out.println("  Patients    : " + currentPatients + " / " + PATIENT_CAPACITY);
    }
}
