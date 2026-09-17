package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class MedicalIncident extends Incident {
    private int patientCount;
    private int criticalPatients;

    public MedicalIncident(String id, String description, double distanceFromBase, int severity,
                           int patientCount, int criticalPatients) throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null, patientCount, criticalPatients);
    }

    public MedicalIncident(String id, String description, double distanceFromBase, int severity,
                           String status, String assignedUnitId,
                           int patientCount, int criticalPatients) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        if (patientCount < 0) {
            throw new InvalidOperationException("Patient count cannot be negative.");
        }
        if (criticalPatients < 0 || criticalPatients > patientCount) {
            throw new InvalidOperationException("Critical patients cannot exceed patient count.");
        }
        this.patientCount = patientCount;
        this.criticalPatients = criticalPatients;
    }

    public int getPatientCount() {
        return patientCount;
    }

    public int getCriticalPatients() {
        return criticalPatients;
    }

    @Override
    public String getRequiredCapability() {
        return "MEDICAL";
    }

    @Override
    public double getPriorityWeight() {
        return 6.0 * getSeverity() + 3.0 * criticalPatients;
    }

    @Override
    public double getWorkload() {
        return patientCount;
    }

    @Override
    public String getTypeName() {
        return "MEDICAL";
    }

    @Override
    public String extraCsvFields() {
        return "," + patientCount + "," + criticalPatients;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Patients    : " + patientCount + " (critical " + criticalPatients + ")");
    }
}
