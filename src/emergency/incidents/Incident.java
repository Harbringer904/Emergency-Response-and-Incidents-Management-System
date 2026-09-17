package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class Incident {
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_RESOLVED = "RESOLVED";

    private String id;
    private String description;
    private double distanceFromBase;
    private int severity;
    private String status;
    private String assignedUnitId;

    public Incident(String id, String description, double distanceFromBase, int severity)
            throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null);
    }

    public Incident(String id, String description, double distanceFromBase, int severity,
                    String status, String assignedUnitId) throws InvalidOperationException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidOperationException("Incident ID cannot be null or empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidOperationException("Description cannot be null or empty.");
        }
        if (distanceFromBase < 0) {
            throw new InvalidOperationException("Distance from base cannot be negative.");
        }
        if (severity < 1 || severity > 5) {
            throw new InvalidOperationException("Severity must be between 1 and 5.");
        }
        if (status == null
                || (!status.equals(STATUS_OPEN)
                && !status.equals(STATUS_ASSIGNED)
                && !status.equals(STATUS_RESOLVED))) {
            throw new InvalidOperationException("Invalid incident status.");
        }
        this.id = id.trim();
        this.description = description.trim();
        this.distanceFromBase = distanceFromBase;
        this.severity = severity;
        this.status = status;
        this.assignedUnitId = assignedUnitId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getDistanceFromBase() {
        return distanceFromBase;
    }

    public int getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedUnitId() {
        return assignedUnitId;
    }

    public String getRequiredCapability() {
        return "GENERAL";
    }

    public double getPriorityWeight() {
        return severity * 5.0;
    }

    public double getWorkload() {
        return severity;
    }

    public void assignUnit(String unitId) throws InvalidOperationException {
        if (STATUS_RESOLVED.equals(status)) {
            throw new InvalidOperationException("A resolved incident cannot be assigned again.");
        }
        if (STATUS_ASSIGNED.equals(status)) {
            throw new InvalidOperationException("Incident " + id + " is already assigned.");
        }
        if (unitId == null || unitId.trim().isEmpty()) {
            throw new InvalidOperationException("Assigned unit ID cannot be empty.");
        }
        this.assignedUnitId = unitId.trim();
        this.status = STATUS_ASSIGNED;
    }

    public void resolve() throws InvalidOperationException {
        if (!STATUS_ASSIGNED.equals(status)) {
            throw new InvalidOperationException("Only an ASSIGNED incident can be resolved.");
        }
        this.status = STATUS_RESOLVED;
    }

    public String getTypeName() {
        return "GENERAL";
    }

    public String extraCsvFields() {
        return "";
    }

    public void display() {
        System.out.println("Incident " + id + " [" + getTypeName() + "]");
        System.out.println("  Description : " + description);
        System.out.println("  Distance    : " + distanceFromBase + " km");
        System.out.println("  Severity    : " + severity);
        System.out.println("  Status      : " + status);
        System.out.println("  Capability  : " + getRequiredCapability());
        System.out.println("  Priority    : " + getPriorityWeight());
        System.out.println("  Workload    : " + getWorkload());
        System.out.println("  Assigned to : " + (assignedUnitId == null ? "-" : assignedUnitId));
    }
}
