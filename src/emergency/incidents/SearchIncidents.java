public class SearchIncidents extends Incident {
    private int missingPersons;
    private double searchArea;

    public SearchIncidents(String id, String description, double distanceFromBase, int severity, String status, String assignedUnitId, String searchArea, String searchType){
        super(id, description, distanceFromBase, severity, status, assignedUnitId);
        this.searchArea = searchArea;
        this.missingPersons = missingPersons;
    }

    @Override
    public String getRequiredCapability(){
        return "SEARCH";
    }

    @Override
    public double getPriorityWeight(){
        return 4.0*getSeverity() + 2.0*missingPersons;
    }

    @Override
    public double getWorkload(){
        return 1.5*searchArea;
    }
}