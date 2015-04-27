package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "edison.graceful.shutdown.active", havingValue = "true")
public class GracefulShutdownStatusIndicator extends MutableStatusDetailIndicator implements SmartLifecycle {

    public static final Logger LOG = LoggerFactory.getLogger(GracefulShutdownStatusIndicator.class);
    private static final String GRACEFUL_SHUTDOWN_KEY = "graceful.shutdown";

    @Value("${edison.graceful.shutdown.time.beforeIndicateError:5000}")
    protected long timeBeforeIndicateError;

    @Value("${edison.graceful.shutdown.time.phaseOut:25000}")
    protected long timeForPhaseOut;

    public GracefulShutdownStatusIndicator() {
        super(StatusDetail.statusDetail(GRACEFUL_SHUTDOWN_KEY, Status.OK, "ready"));
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            toOk("graceful shutdown signal received");
            waitForIndicateError();

            LOG.info("indicating ERROR status");
            toError("phasing out ..");

            waitForShutdown();
            toError("regular shutdown started");
        } catch (InterruptedException e) {
            LOG.error("graceful shutdown interrupted", e);
        }
        callback.run();
    }

    void waitForShutdown() throws InterruptedException {
        Thread.sleep(timeForPhaseOut);
        LOG.info("grace period ended, starting shutdown now");
    }

    void waitForIndicateError() throws InterruptedException {
        LOG.info("shutdown signal received ...");
        Thread.sleep(timeBeforeIndicateError);
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
}
