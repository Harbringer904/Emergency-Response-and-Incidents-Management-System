package emergency.capabilities;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public interface WaterCarrier {
    void refillWater(double amount) throws InvalidOperationException;

    void useWater(double amount) throws InsufficientResourceException, InvalidOperationException;

    double getWaterLevel();

    double getWaterCapacity();
}
