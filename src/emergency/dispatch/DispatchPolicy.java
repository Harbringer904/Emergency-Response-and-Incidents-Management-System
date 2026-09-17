package emergency.dispatch;

public class DispatchPolicy {
    private double lowFuelThreshold;
    private double lowFuelPenalty;
    private double lowBatteryThreshold;
    private double lowBatteryPenalty;
    private double lowWaterThreshold;
    private double lowWaterPenalty;
    private double lowSupplyThreshold;
    private double lowSupplyPenalty;

    public DispatchPolicy() {
        this.lowFuelThreshold = 0.25;
        this.lowFuelPenalty = 12;
        this.lowBatteryThreshold = 0.30;
        this.lowBatteryPenalty = 15;
        this.lowWaterThreshold = 0.40;
        this.lowWaterPenalty = 20;
        this.lowSupplyThreshold = 0.25;
        this.lowSupplyPenalty = 10;
    }

    public DispatchPolicy(double lowFuelThreshold, double lowFuelPenalty,
                          double lowBatteryThreshold, double lowBatteryPenalty,
                          double lowWaterThreshold, double lowWaterPenalty,
                          double lowSupplyThreshold, double lowSupplyPenalty) {
        this.lowFuelThreshold = lowFuelThreshold;
        this.lowFuelPenalty = lowFuelPenalty;
        this.lowBatteryThreshold = lowBatteryThreshold;
        this.lowBatteryPenalty = lowBatteryPenalty;
        this.lowWaterThreshold = lowWaterThreshold;
        this.lowWaterPenalty = lowWaterPenalty;
        this.lowSupplyThreshold = lowSupplyThreshold;
        this.lowSupplyPenalty = lowSupplyPenalty;
    }

    public double getLowFuelThreshold() {
        return lowFuelThreshold;
    }

    public double getLowFuelPenalty() {
        return lowFuelPenalty;
    }

    public double getLowBatteryThreshold() {
        return lowBatteryThreshold;
    }

    public double getLowBatteryPenalty() {
        return lowBatteryPenalty;
    }

    public double getLowWaterThreshold() {
        return lowWaterThreshold;
    }

    public double getLowWaterPenalty() {
        return lowWaterPenalty;
    }

    public double getLowSupplyThreshold() {
        return lowSupplyThreshold;
    }

    public double getLowSupplyPenalty() {
        return lowSupplyPenalty;
    }

    public void setLowFuelThreshold(double lowFuelThreshold) {
        this.lowFuelThreshold = lowFuelThreshold;
    }

    public void setLowFuelPenalty(double lowFuelPenalty) {
        this.lowFuelPenalty = lowFuelPenalty;
    }

    public void setLowBatteryThreshold(double lowBatteryThreshold) {
        this.lowBatteryThreshold = lowBatteryThreshold;
    }

    public void setLowBatteryPenalty(double lowBatteryPenalty) {
        this.lowBatteryPenalty = lowBatteryPenalty;
    }

    public void setLowWaterThreshold(double lowWaterThreshold) {
        this.lowWaterThreshold = lowWaterThreshold;
    }

    public void setLowWaterPenalty(double lowWaterPenalty) {
        this.lowWaterPenalty = lowWaterPenalty;
    }

    public void setLowSupplyThreshold(double lowSupplyThreshold) {
        this.lowSupplyThreshold = lowSupplyThreshold;
    }

    public void setLowSupplyPenalty(double lowSupplyPenalty) {
        this.lowSupplyPenalty = lowSupplyPenalty;
    }
}
