

public class ResponseUnit {
    private String id;
    private String name;
    private double maxSpeed;
    private double totalDistanceTravelled;
    private boolean available;
    private String assignedIncidentId;
    private int completedIncidents;

    public ResponseUnit(String id, String name, double maxspeed){
        if (id == null || name == null || maxspeed <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        this.id = id;
        this.name = name;
        this.maxSpeed = maxspeed;
        this.totalDistanceTravelled = 0;
        this.available = true;
        this.assignedIncidentId = null;
        this.completedIncidents = 0;
    }

    public void move(double distance){
        if (distance <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }
        this.totalDistanceTravelled += distance;
        
        System.out.println(this.name + " moved " + distance + " kms");
    }

    public double estimateArrivalTime(double distance){
        return distance / this.maxSpeed;

        System.out.println(this.name + " will arrive in " + arrivalTime + " hours");
    }

    public String getCapability(){
        return "GENERAL";
    }

    public boolean canHandle(Incident incident){
        /**add needed to be the getCapability() of the incident */
        if (GENERAL.equals(needed)){
             return true;
        }
        return false;
    }
    public boolean hasResourceFor(Incident incident){
        return true;
    }

    public double calculateDispatchScore(Incident incident, DispatchPolicy policy){
        double minutes = estimateArrivalTime(incident.getDistanceFromBase()) * 60.0;
        return minutes - incident.getPriorityWeight();
    }
}
