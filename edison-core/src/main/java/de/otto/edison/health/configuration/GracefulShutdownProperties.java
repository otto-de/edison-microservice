package de.otto.edison.health.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties used to configure the graceful shutdown of Edison microservices.
 */
@Configuration
@ConfigurationProperties(prefix = "edison.gracefulshutdown")
public class GracefulShutdownProperties {

    /**
     * Enable/Disable graceful shutdown.
     * <p>
     *     Disabling the shutdown is especially required in test cases, where you do not want to wait for 25sec. to stop
     *     a test server instance.
     * </p>
     */
    public final boolean enabled = true;

    /**
     * Milliseconds to wait before /internal/health is starting to respond with server errors,
     * after shutdown signal is retrieved.
     * <p> Default:
     *     {@code edison.gracefulshutdown.indicateErrorAfter = 5000}
     * </p>
     */
    public final long indicateErrorAfter = 5000L;

    /**
     * Milliseconds to respond /internal/health checks with server errors, before actually shutting down the application.
     * <p> Default:
     *     edison.gracefulshutdown.phaseOutAfter = 20000
     * </p>
     */
    public final long phaseOutAfter = 20000L;

}
