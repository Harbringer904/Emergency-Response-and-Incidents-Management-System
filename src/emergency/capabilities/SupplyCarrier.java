package emergency.capabilities;

import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;

public interface SupplyCarrier {
    void loadSupplies(int number) throws InvalidOperationException;

    void useSupplies(int number) throws InsufficientResourceException, InvalidOperationException;

    int getSupplyLevel();

    int getSupplyCapacity();
}
