package de.otto.edison.health.indicator;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.SmartLifecycle;

import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

@ThreadSafe
public class GracefulShutdownHealthIndicator implements SmartLifecycle, HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(GracefulShutdownHealthIndicator.class);
    private static final Marker SHUTDOWN_MARKER = MarkerFactory.getMarker("EDISON_SHUTDOWN");

    private final GracefulShutdownProperties properties;

    private volatile Health health = up().build();

    public GracefulShutdownHealthIndicator(final GracefulShutdownProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        return health;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            waitForSettingHealthCheckToDown();

            LOG.info(SHUTDOWN_MARKER, "set health check to down");
            indicateDown();

            waitForShutdown();
        } catch (InterruptedException e) {
            LOG.error(SHUTDOWN_MARKER, "graceful shutdown interrupted", e);
        } finally {
            callback.run();
        }
    }

    private void indicateDown() {
        health = down().build();
    }

    void waitForSettingHealthCheckToDown() throws InterruptedException {
        LOG.info(SHUTDOWN_MARKER, "shutdown signal received ...");
        Thread.sleep(properties.indicateErrorAfter);
    }

    void waitForShutdown() throws InterruptedException {
        Thread.sleep(properties.phaseOutAfter);
        LOG.info(SHUTDOWN_MARKER, "grace period ended, starting shutdown now");
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
