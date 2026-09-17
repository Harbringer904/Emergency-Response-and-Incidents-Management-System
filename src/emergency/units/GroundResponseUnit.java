public class GroundResponseUnit extends ResponseUnit {
    /** cannot be less than 1.0, 
        1.00 = no traffic delay
        1.20 = 20% additional travel time
        1.50 = 50% additional travel time
 */
    private double trafficFactor;

    @Override
    public double estimateArrivalTime(double distance){
        return (distance / getMaxSpeed()) * trafficFactor;
    }

    @Override
    public void move(double distance){
        
    }
}