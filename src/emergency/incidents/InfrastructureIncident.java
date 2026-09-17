package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class InfrastructureIncident extends Incident {
    private int affectedUsers;
    private boolean criticalService;

    public InfrastructureIncident(String id, String description, double distanceFromBase, int severity,
                                  int affectedUsers, boolean criticalService) throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null, affectedUsers, criticalService);
    }

    public InfrastructureIncident(String id, String description, double distanceFromBase, int severity,
                                  String status, String assignedUnitId,
                                  int affectedUsers, boolean criticalService) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        if (affectedUsers < 0) {
            throw new InvalidOperationException("Affected users cannot be negative.");
        }
        this.affectedUsers = affectedUsers;
        this.criticalService = criticalService;
    }

    public int getAffectedUsers() {
        return affectedUsers;
    }

    public boolean isCriticalService() {
        return criticalService;
    }

    @Override
    public String getRequiredCapability() {
        return "REPAIR";
    }

    @Override
    public double getPriorityWeight() {
        double userComponent = Math.min(10.0, affectedUsers / 100.0);
        double criticalExtra = criticalService ? 10.0 : 0.0;
        return 5.0 * getSeverity() + criticalExtra + userComponent;
    }

    @Override
    public double getWorkload() {
        return Math.ceil(getSeverity() + affectedUsers / 500.0);
    }

    @Override
    public String getTypeName() {
        return "INFRASTRUCTURE";
    }

    @Override
    public String extraCsvFields() {
        return "," + affectedUsers + "," + criticalService;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Users       : " + affectedUsers);
        System.out.println("  Critical    : " + criticalService);
    }
}
