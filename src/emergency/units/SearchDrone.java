package emergency.units;

import emergency.capabilities.BatteryPowered;
import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class SearchDrone extends ResponseUnit implements BatteryPowered {
    public static final double BATTERY_CAPACITY = 100.0;
    public static final double BATTERY_PER_KM = 0.6;

    private double batteryLevel;

    public SearchDrone(String id, String name, double maxSpeed) throws InvalidOperationException {
        this(id, name, maxSpeed, BATTERY_CAPACITY, 0.0, true, null, 0);
    }

    public SearchDrone(String id, String name, double maxSpeed, double batteryLevel,
                       double totalDistanceTravelled, boolean available,
                       String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, totalDistanceTravelled, available, assignedIncidentId, completedIncidents);
        if (batteryLevel < 0 || batteryLevel > BATTERY_CAPACITY) {
            throw new InvalidOperationException("Battery level out of range.");
        }
        this.batteryLevel = batteryLevel;
    }

    @Override
    public String getCapability() {
        return "SEARCH";
    }

    @Override
    public double estimateArrivalTime(double distance) {
        return super.estimateArrivalTime(distance) * 0.85;
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        double travelCost = distance * BATTERY_PER_KM;
        consumeBattery(travelCost);
        recordDistance(distance);
        System.out.println("Search drone " + getId() + " flying " + distance + " km.");
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        double distance = incident.getDistanceFromBase();
        double travel = 2.0 * distance * BATTERY_PER_KM;
        double search = 0.0;
        if ("SEARCH".equals(incident.getRequiredCapability())) {
            search = incident.getWorkload();
        }
        return hasBatteryFor(travel + search);
    }

    @Override
    public String resourceFailureReason(Incident incident) {
        return "Insufficient battery";
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);
        if (batteryLevel < policy.getLowBatteryThreshold() * BATTERY_CAPACITY) {
            score += policy.getLowBatteryPenalty();
        }
        return score;
    }

    @Override
    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        if ("SEARCH".equals(incident.getRequiredCapability())) {
            consumeBattery(incident.getWorkload());
        }
    }

    @Override
    public void recharge(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Recharge amount must be positive.");
        }
        batteryLevel = Math.min(BATTERY_CAPACITY, batteryLevel + amount);
    }

    @Override
    public double getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public double getBatteryCapacity() {
        return BATTERY_CAPACITY;
    }

    @Override
    public double consumeBattery(double amount) throws InsufficientResourceException {
        if (amount < 0) {
            throw new InsufficientResourceException("Battery amount cannot be negative.");
        }
        if (batteryLevel + 1e-9 < amount) {
            throw new InsufficientResourceException("Insufficient battery.");
        }
        batteryLevel -= amount;
        return amount;
    }

    @Override
    public boolean hasBatteryFor(double amount) {
        return batteryLevel + 1e-9 >= amount;
    }

    @Override
    public String getTypeName() {
        return "SEARCH_DRONE";
    }

    @Override
    public String extraCsvFields() {
        return "," + batteryLevel;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Battery     : " + batteryLevel + " / " + BATTERY_CAPACITY);
    }
}
