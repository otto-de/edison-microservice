package de.otto.edison.metrics.load;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * Configuration used to configure the {@link LoadDetector} of Edison services.
 */
@ConfigurationProperties(prefix = "edison.metrics.load")
@Validated
public class MetricsLoadProperties {

    /**
     * Enable / disable support for load detection / auto scaling of services.
     */
    private boolean enabled = false;
    /**
     * The name of the {@link com.codahale.metrics.Timer timer} to use for calculating load metrics.
     */
    @NotEmpty
    private String timerName;

    /**
     * The MIN threshold. If the selected metric is less than this threshold, the {@link LoadDetector} is reporting
     * the system to be {@link LoadDetector.Status#IDLE}
     */
    @Min(0)
    private double minThreshold;

    /**
     * The MAX threshold. If the selected metric is higher than this value, the {@link LoadDetector} is reporting
     * {@link LoadDetector.Status#OVERLOAD}
     */
    @Min(0)
    private double maxThreshold;

    private Class<? extends LoadDetector> strategy;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
