package emergency.units;

import emergency.capabilities.FuelPowered;
import emergency.capabilities.SupplyCarrier;
import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class RepairVan extends GroundResponseUnit implements FuelPowered, SupplyCarrier {
    public static final double FUEL_CAPACITY = 100.0;
    public static final double KM_PER_LITRE = 10.0;
    public static final int SUPPLY_CAPACITY = 30;

    private double fuelLevel;
    private int supplyLevel;

    public RepairVan(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        this(id, name, maxSpeed, trafficFactor, FUEL_CAPACITY, SUPPLY_CAPACITY, 0.0, true, null, 0);
    }

    public RepairVan(String id, String name, double maxSpeed, double trafficFactor,
                     double fuelLevel, int supplyLevel, double totalDistanceTravelled,
                     boolean available, String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor, totalDistanceTravelled, available,
                assignedIncidentId, completedIncidents);
        if (fuelLevel < 0 || fuelLevel > FUEL_CAPACITY) {
            throw new InvalidOperationException("Fuel level out of range.");
        }
        if (supplyLevel < 0 || supplyLevel > SUPPLY_CAPACITY) {
            throw new InvalidOperationException("Supply level out of range.");
        }
        this.fuelLevel = fuelLevel;
        this.supplyLevel = supplyLevel;
    }

    @Override
    public String getCapability() {
        return "REPAIR";
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        recordDistance(distance);
        System.out.println("Repair van " + getId() + " travelling " + distance + " km by road.");
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return false;
        }
        if ("REPAIR".equals(incident.getRequiredCapability())) {
            int needed = (int) Math.round(incident.getWorkload());
            return supplyLevel >= needed;
        }
        return true;
    }

    @Override
    public String resourceFailureReason(Incident incident) {
        double roundTrip = 2.0 * incident.getDistanceFromBase();
        if (!hasFuelFor(roundTrip)) {
            return "Insufficient fuel";
        }
        if ("REPAIR".equals(incident.getRequiredCapability())) {
            return "Insufficient supplies";
        }
        return "Insufficient resources";
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);
        if (fuelLevel < policy.getLowFuelThreshold() * FUEL_CAPACITY) {
            score += policy.getLowFuelPenalty();
        }
        if (supplyLevel < policy.getLowSupplyThreshold() * SUPPLY_CAPACITY) {
            score += policy.getLowSupplyPenalty();
        }
        return score;
    }

    @Override
    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        if ("REPAIR".equals(incident.getRequiredCapability())) {
            useSupplies((int) Math.round(incident.getWorkload()));
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
    public void loadSupplies(int number) throws InvalidOperationException {
        if (number <= 0) {
            throw new InvalidOperationException("Supply load amount must be positive.");
        }
        int next = supplyLevel + number;
        if (next > SUPPLY_CAPACITY) {
            supplyLevel = SUPPLY_CAPACITY;
        } else {
            supplyLevel = next;
        }
    }

    @Override
    public void useSupplies(int number) throws InsufficientResourceException, InvalidOperationException {
        if (number < 0) {
            throw new InvalidOperationException("Supply amount cannot be negative.");
        }
        if (supplyLevel < number) {
            throw new InsufficientResourceException("Insufficient supplies.");
        }
        supplyLevel -= number;
    }

    @Override
    public int getSupplyLevel() {
        return supplyLevel;
    }

    @Override
    public int getSupplyCapacity() {
        return SUPPLY_CAPACITY;
    }

    @Override
    public String getTypeName() {
        return "REPAIR_VAN";
    }

    @Override
    public String extraCsvFields() {
        return "," + getTrafficFactor() + "," + fuelLevel + "," + supplyLevel;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Fuel        : " + fuelLevel + " / " + FUEL_CAPACITY + " L");
        System.out.println("  Supplies    : " + supplyLevel + " / " + SUPPLY_CAPACITY);
    }
}
