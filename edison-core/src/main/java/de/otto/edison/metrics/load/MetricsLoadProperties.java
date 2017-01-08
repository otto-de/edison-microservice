package de.otto.edison.metrics.load;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.metrics.load")
public class MetricsLoadProperties {

    private String timerName;

    private double minThreshold;

    private double maxThreshold;

    private Class<? extends LoadDetector> strategy;

    public String getTimerName() {
        return this.timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public double getMinThreshold() {
        return this.minThreshold;
    }

    public void setMinThreshold(double minThreshold) {
        this.minThreshold = minThreshold;
    }

    public double getMaxThreshold() {
        return this.maxThreshold;
    }

    public void setMaxThreshold(double maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public Class<? extends LoadDetector> getStrategy() {
        return this.strategy;
    }

    public void setStrategy(Class<? extends LoadDetector> strategy) {
        this.strategy = strategy;
    }
}
