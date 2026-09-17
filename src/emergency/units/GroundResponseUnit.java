package emergency.units;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public class GroundResponseUnit extends ResponseUnit {
    private double trafficFactor;

    public GroundResponseUnit(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        this(id, name, maxSpeed, trafficFactor, 0.0, true, null, 0);
    }

    public GroundResponseUnit(String id, String name, double maxSpeed, double trafficFactor,
                              double totalDistanceTravelled, boolean available,
                              String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        super(id, name, maxSpeed, totalDistanceTravelled, available, assignedIncidentId, completedIncidents);
        if (trafficFactor < 1.0) {
            throw new InvalidOperationException("Traffic factor must be at least 1.0.");
        }
        this.trafficFactor = trafficFactor;
    }

    public double getTrafficFactor() {
        return trafficFactor;
    }

    @Override
    public double estimateArrivalTime(double distance) {
        return (distance / getMaxSpeed()) * trafficFactor;
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        recordDistance(distance);
        System.out.println("Unit " + getId() + " travelling " + distance
                + " km by road (traffic factor " + trafficFactor + ").");
    }

    @Override
    public String getTypeName() {
        return "GROUND";
    }

    @Override
    public String extraCsvFields() {
        return "," + trafficFactor;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Traffic     : " + trafficFactor);
    }
}
