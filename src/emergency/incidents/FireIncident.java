package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class FireIncident extends Incident {
    private double affectedArea;
    private boolean hazardousMaterial;

    public FireIncident(String id, String description, double distanceFromBase, int severity,
                        double affectedArea, boolean hazardousMaterial) throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null, affectedArea, hazardousMaterial);
    }

    public FireIncident(String id, String description, double distanceFromBase, int severity,
                        String status, String assignedUnitId,
                        double affectedArea, boolean hazardousMaterial) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        if (affectedArea < 0) {
            throw new InvalidOperationException("Affected area cannot be negative.");
        }
        this.affectedArea = affectedArea;
        this.hazardousMaterial = hazardousMaterial;
    }

    public double getAffectedArea() {
        return affectedArea;
    }

    public boolean hasHazardousMaterial() {
        return hazardousMaterial;
    }

    @Override
    public String getRequiredCapability() {
        return "FIRE";
    }

    @Override
    public double getPriorityWeight() {
        double extra = hazardousMaterial ? 15.0 : 0.0;
        return 7.0 * getSeverity() + extra;
    }

    @Override
    public double getWorkload() {
        return 300.0 * getSeverity() + 0.2 * affectedArea;
    }

    @Override
    public String getTypeName() {
        return "FIRE";
    }

    @Override
    public String extraCsvFields() {
        return "," + affectedArea + "," + hazardousMaterial;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Area        : " + affectedArea);
        System.out.println("  Hazardous   : " + hazardousMaterial);
    }
}
