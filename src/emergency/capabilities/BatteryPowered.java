package emergency.capabilities;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public interface BatteryPowered {
    void recharge(double amount) throws InvalidOperationException;

    double getBatteryLevel();

    double getBatteryCapacity();

    double consumeBattery(double amount) throws InsufficientResourceException;

    boolean hasBatteryFor(double amount);
}
