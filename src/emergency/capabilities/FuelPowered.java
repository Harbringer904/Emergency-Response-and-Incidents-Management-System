package emergency.capabilities;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public interface FuelPowered {
    void refuel(double amount) throws InvalidOperationException;

    double getFuelLevel();

    double getFuelCapacity();

    double consumeFuel(double distance) throws InsufficientResourceException;

    boolean hasFuelFor(double distance);
}
