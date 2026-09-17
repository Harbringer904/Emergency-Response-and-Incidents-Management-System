package emergency.units;

import emergency.dispatch.DispatchPolicy;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.incidents.Incident;

public class ResponseUnit {
    private String id;
    private String name;
    private double maxSpeed;
    private double totalDistanceTravelled;
    private boolean available;
    private String assignedIncidentId;
    private int completedIncidents;

    public ResponseUnit(String id, String name, double maxSpeed) throws InvalidOperationException {
        this(id, name, maxSpeed, 0.0, true, null, 0);
    }

    public ResponseUnit(String id, String name, double maxSpeed, double totalDistanceTravelled,
                        boolean available, String assignedIncidentId, int completedIncidents)
            throws InvalidOperationException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidOperationException("Unit ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOperationException("Unit name cannot be null or empty.");
        }
        if (maxSpeed <= 0) {
            throw new InvalidOperationException("Maximum speed must be greater than zero.");
        }
        if (totalDistanceTravelled < 0) {
            throw new InvalidOperationException("Total distance cannot be negative.");
        }
        if (completedIncidents < 0) {
            throw new InvalidOperationException("Completed incidents cannot be negative.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.maxSpeed = maxSpeed;
        this.totalDistanceTravelled = totalDistanceTravelled;
        this.available = available;
        this.assignedIncidentId = assignedIncidentId;
        this.completedIncidents = completedIncidents;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getTotalDistanceTravelled() {
        return totalDistanceTravelled;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getAssignedIncidentId() {
        return assignedIncidentId;
    }

    public int getCompletedIncidents() {
        return completedIncidents;
    }

    protected void recordDistance(double distance) throws InvalidOperationException {
        if (distance < 0) {
            throw new InvalidOperationException("Distance cannot be negative.");
        }
        totalDistanceTravelled += distance;
    }

    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        recordDistance(distance);
        System.out.println("Unit " + id + " travelling " + distance + " km.");
    }

    public double estimateArrivalTime(double distance) {
        return distance / maxSpeed;
    }

    public String getCapability() {
        return "GENERAL";
    }

    public boolean canHandle(Incident incident) {
        String needed = incident.getRequiredCapability();
        if ("GENERAL".equals(needed)) {
            return true;
        }
        return getCapability().equals(needed);
    }

    public boolean hasResourcesFor(Incident incident) {
        return true;
    }

    public String resourceFailureReason(Incident incident) {
        return "Insufficient resources";
    }

    public String ineligibilityReason(Incident incident) {
        if (!available) {
            return "Currently assigned";
        }
        if (!canHandle(incident)) {
            return "Capability mismatch";
        }
        if (!hasResourcesFor(incident)) {
            return resourceFailureReason(incident);
        }
        return null;
    }

    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double minutes = estimateArrivalTime(incident.getDistanceFromBase()) * 60.0;
        return minutes - incident.getPriorityWeight();
    }

    public void assignIncident(String incidentId) throws InvalidOperationException {
        if (!available) {
            throw new InvalidOperationException("Unit " + id + " is already assigned.");
        }
        if (incidentId == null || incidentId.trim().isEmpty()) {
            throw new InvalidOperationException("Incident ID cannot be empty.");
        }
        this.assignedIncidentId = incidentId.trim();
        this.available = false;
    }

    public void releaseIncident() {
        this.assignedIncidentId = null;
        this.available = true;
    }

    public void incrementCompletedIncidents() {
        completedIncidents++;
    }

    public void performWork(Incident incident)
            throws InvalidOperationException, InsufficientResourceException {
        // Base units have no extra operational resource to spend.
    }

    public String getTypeName() {
        return "RESPONSE_UNIT";
    }

    public String extraCsvFields() {
        return "";
    }

    public String toCsvRecord() {
        String assigned = assignedIncidentId == null ? "" : assignedIncidentId;
        return getTypeName() + "," + id + "," + escape(name) + "," + maxSpeed + ","
                + totalDistanceTravelled + "," + available + "," + assigned + ","
                + completedIncidents + extraCsvFields();
    }

    protected static String escape(String text) {
        return text.replace(",", ";");
    }

    public void display() {
        System.out.println("Unit " + id + " [" + getTypeName() + "] " + name);
        System.out.println("  Capability  : " + getCapability());
        System.out.println("  Speed       : " + maxSpeed + " km/h");
        System.out.println("  Distance    : " + totalDistanceTravelled + " km");
        System.out.println("  Available   : " + available);
        System.out.println("  Assigned to : " + (assignedIncidentId == null ? "-" : assignedIncidentId));
        System.out.println("  Completed   : " + completedIncidents);
    }
}
