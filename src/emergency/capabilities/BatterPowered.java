public interface BatteryPowered {
    void recharge(double amount);
    double getBatteryLevel();
    double getBatteryCapacity();
    double consumeBattery(double distance);
    boolean hasBatteryFor(double distance);
}