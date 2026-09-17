package emergency.capabilities;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public interface FuelPowered {
    void refuel(double amount) throws InvalidOperationException;

    double getFuelLevel();

    double getFuelCapacity();

    double consumeFuel(double distance) throws InsufficientResourceException, InvalidOperationException;

    boolean hasFuelFor(double distance);
}
