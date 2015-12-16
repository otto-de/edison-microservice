package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.*;

public interface JobEventListener {

    void consumeStarted(StartedEvent startedEvent);

    void consumeStopped(StoppedEvent stoppedEvent);

    void consumeError(ErrorEvent errorEvent);

    void consumeInfo(InfoEvent infoEvent);

    void consumePing(PingEvent pingEvent);
}
