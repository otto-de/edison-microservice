package de.otto.edison.health.indicator;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.SmartLifecycle;

import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

@ThreadSafe
public class GracefulShutdownHealthIndicator implements SmartLifecycle, HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(GracefulShutdownHealthIndicator.class);

    private final long timeBeforeIndicateError;
    private final long timeForPhaseOut;

    private Health health = up().build();

    public GracefulShutdownHealthIndicator(final long timeBeforeIndicateError, final long timeForPhaseOut) {
        this.timeBeforeIndicateError = timeBeforeIndicateError;
        this.timeForPhaseOut = timeForPhaseOut;
    }

    @Override
    public Health health() {
        return health;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            waitForSettingHealthCheckToDown();

            LOG.info("set health check to down");
            indicateDown();

            waitForShutdown();
        } catch (InterruptedException e) {
            LOG.error("graceful shutdown interrupted", e);
        } finally {
            callback.run();
        }
    }

    private void indicateDown() {
        health = down().build();
    }

    void waitForSettingHealthCheckToDown() throws InterruptedException {
        LOG.info("shutdown signal received ...");
        Thread.sleep(timeBeforeIndicateError);
    }

    void waitForShutdown() throws InterruptedException {
        Thread.sleep(timeForPhaseOut);
        LOG.info("grace period ended, starting shutdown now");
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
