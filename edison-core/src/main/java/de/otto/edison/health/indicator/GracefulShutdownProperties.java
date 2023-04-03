package de.otto.edison.health.indicator;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties used to configure the graceful shutdown of Edison microservices.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.gracefulshutdown")
@Validated
public class GracefulShutdownProperties {

    /**
     * Enable/Disable graceful shutdown.
     * <p>
     *     Disabling the shutdown is especially required in test cases, where you do not want to wait for 25sec. to stop
     *     a test server instance.
     * </p>
     */
    private boolean enabled = false;

    /**
     * Milliseconds to wait before /internal/health is starting to respond with server errors,
     * after shutdown signal is retrieved.
     * <p> Default:
     *     {@code edison.gracefulshutdown.indicateErrorAfter = 5000}
     * </p>
     */
    @Min(0)
    private long indicateErrorAfter = 5000L;

    /**
     * Milliseconds to respond /internal/health checks with server errors, before actually shutting down the application.
     * <p> Default:
     *     edison.gracefulshutdown.phaseOutAfter = 20000
     * </p>
     */
    @Min(100)
    private long phaseOutAfter = 20000L;

    public boolean isEnabled() {
        return enabled;
    }

    public long getIndicateErrorAfter() {
        return indicateErrorAfter;
    }

    public long getPhaseOutAfter() {
        return phaseOutAfter;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIndicateErrorAfter(long indicateErrorAfter) {
        this.indicateErrorAfter = indicateErrorAfter;
    }

    public void setPhaseOutAfter(long phaseOutAfter) {
        this.phaseOutAfter = phaseOutAfter;
    }
}
