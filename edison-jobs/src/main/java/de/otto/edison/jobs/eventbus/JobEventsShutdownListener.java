package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.Level;
import org.springframework.context.SmartLifecycle;

public class JobEventsShutdownListener implements SmartLifecycle {

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        JobEvents.broadcast(Level.ERROR, "Service is shutting down, this job will (likely) be cancelled.");
        callback.run();
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
        //This lifecycle component should be called as early as possible to log into all running jobs.
        return Integer.MAX_VALUE;
    }
}
