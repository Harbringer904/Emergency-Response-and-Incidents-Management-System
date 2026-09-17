package emergency.incidents;

import emergency.exceptions.InvalidOperationException;

public class SearchIncident extends Incident {
    private int missingPersons;
    private double searchArea;

    public SearchIncident(String id, String description, double distanceFromBase, int severity,
                          int missingPersons, double searchArea) throws InvalidOperationException {
        this(id, description, distanceFromBase, severity, STATUS_OPEN, null, missingPersons, searchArea);
    }

    public SearchIncident(String id, String description, double distanceFromBase, int severity,
                          String status, String assignedUnitId,
                          int missingPersons, double searchArea) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        if (missingPersons < 0) {
            throw new InvalidOperationException("Missing persons cannot be negative.");
        }
        if (searchArea < 0) {
            throw new InvalidOperationException("Search area cannot be negative.");
        }
        this.missingPersons = missingPersons;
        this.searchArea = searchArea;
    }

    public int getMissingPersons() {
        return missingPersons;
    }

    public double getSearchArea() {
        return searchArea;
    }

    @Override
    public String getRequiredCapability() {
        return "SEARCH";
    }

    @Override
    public double getPriorityWeight() {
        return 4.0 * getSeverity() + 2.0 * missingPersons;
    }

    @Override
    public double getWorkload() {
        return 1.5 * searchArea;
    }

    @Override
    public String getTypeName() {
        return "SEARCH";
    }

    @Override
    public String extraCsvFields() {
        return "," + missingPersons + "," + searchArea;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Missing     : " + missingPersons);
        System.out.println("  Search area : " + searchArea);
    }
}
