package emergency.units;

import emergency.capabilities.FuelPowered;
import emergency.capabilities.WaterCarrier;
import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class FireEngine extends GroundResponseUnit implements FuelPowered, WaterCarrier {
    public static final double FUEL_CAPACITY = 150.0;
    public static final double KM_PER_LITRE = 5.0;
    public static final double WATER_CAPACITY = 3000.0;

    private double fuelLevel;
    private double waterLevel;

    public FireEngine(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        this(id, name, maxSpeed, trafficFactor, FUEL_CAPACITY, WATER_CAPACITY, 0.0, true, null, 0);
    }

    public FireEngine(String id, String name, double maxSpeed, double trafficFactor,
                      double fuelLevel, double waterLevel, double totalDistanceTravelled,
                      boolean available, String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor, totalDistanceTravelled, available,
                assignedIncidentId, completedIncidents);
        if (fuelLevel < 0 || fuelLevel > FUEL_CAPACITY) {
            throw new InvalidOperationException("Fuel level out of range.");
        }
        if (waterLevel < 0 || waterLevel > WATER_CAPACITY) {
            throw new InvalidOperationException("Water level out of range.");
        }
        this.fuelLevel = fuelLevel;
        this.waterLevel = waterLevel;
    }

    @Override
    public String getCapability() {
        return "FIRE";
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        recordDistance(distance);
        System.out.println("Fire engine " + getId() + " travelling " + distance + " km by road.");
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return false;
        }
        if ("FIRE".equals(incident.getRequiredCapability())) {
            return waterLevel + 1e-9 >= incident.getWorkload();
        }
        return true;
    }

    @Override
    public String resourceFailureReason(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return "Insufficient fuel";
        }
        if ("FIRE".equals(incident.getRequiredCapability())) {
            return "Insufficient water";
        }
        return "Insufficient resources";
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);
        if (fuelLevel < policy.getLowFuelThreshold() * FUEL_CAPACITY) {
            score += policy.getLowFuelPenalty();
        }
        if (waterLevel < policy.getLowWaterThreshold() * WATER_CAPACITY) {
            score += policy.getLowWaterPenalty();
        }
        return score;
    }

    @Override
    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        if ("FIRE".equals(incident.getRequiredCapability())) {
            useWater(incident.getWorkload());
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
    public double consumeFuel(double distance) throws InsufficientResourceException {
        double litres = distance / KM_PER_LITRE;
        if (fuelLevel + 1e-9 < litres) {
            throw new InsufficientResourceException("Insufficient fuel for " + distance + " km.");
        }
        fuelLevel -= litres;
        return litres;
    }

    @Override
    public boolean hasFuelFor(double distance) {
        return fuelLevel + 1e-9 >= distance / KM_PER_LITRE;
    }

    @Override
    public void refillWater(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Refill amount must be positive.");
        }
        waterLevel = Math.min(WATER_CAPACITY, waterLevel + amount);
    }

    @Override
    public void useWater(double amount) throws InsufficientResourceException, InvalidOperationException {
        if (amount < 0) {
            throw new InvalidOperationException("Water amount cannot be negative.");
        }
        if (waterLevel + 1e-9 < amount) {
            throw new InsufficientResourceException("Insufficient water.");
        }
        waterLevel -= amount;
    }

    @Override
    public double getWaterLevel() {
        return waterLevel;
    }

    @Override
    public double getWaterCapacity() {
        return WATER_CAPACITY;
    }

    @Override
    public String getTypeName() {
        return "FIRE_ENGINE";
    }

    @Override
    public String extraCsvFields() {
        return "," + getTrafficFactor() + "," + fuelLevel + "," + waterLevel;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Fuel        : " + fuelLevel + " / " + FUEL_CAPACITY + " L");
        System.out.println("  Water       : " + waterLevel + " / " + WATER_CAPACITY + " L");
    }
}
