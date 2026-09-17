package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class EvacuationIncident extends Incident {
    private int peopleToMove;
    private double remainingSafeHours;

    public EvacuationIncident(String id, String description, double distanceFromBase, int severity,
                              int peopleToMove, double remainingSafeHours) throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null, peopleToMove, remainingSafeHours);
    }

    public EvacuationIncident(String id, String description, double distanceFromBase, int severity,
                              String status, String assignedUnitId,
                              int peopleToMove, double remainingSafeHours) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        if (peopleToMove < 0) {
            throw new InvalidOperationException("People to move cannot be negative.");
        }
        if (remainingSafeHours < 0) {
            throw new InvalidOperationException("Remaining safe hours cannot be negative.");
        }
        this.peopleToMove = peopleToMove;
        this.remainingSafeHours = remainingSafeHours;
    }

    public int getPeopleToMove() {
        return peopleToMove;
    }

    public double getRemainingSafeHours() {
        return remainingSafeHours;
    }

    @Override
    public String getRequiredCapability() {
        return "EVACUATION";
    }

    @Override
    public double getPriorityWeight() {
        double urgency = remainingSafeHours < 2.0 ? 20.0 : 0.0;
        return 5.0 * getSeverity() + peopleToMove / 10.0 + urgency;
    }

    @Override
    public double getWorkload() {
        return peopleToMove;
    }

    @Override
    public String getTypeName() {
        return "EVACUATION";
    }

    @Override
    public String extraCsvFields() {
        return "," + peopleToMove + "," + remainingSafeHours;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  People      : " + peopleToMove);
        System.out.println("  Safe hours  : " + remainingSafeHours);
    }
}
