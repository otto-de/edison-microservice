package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.ERROR;
import static java.time.Instant.ofEpochMilli;
import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;

public class PersistenceJobStateChangeListener implements JobStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceJobStateChangeListener.class);

    private final JobService jobService;

    public PersistenceJobStateChangeListener(final JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void consumeStateChange(final StateChangeEvent event) {
        try {
            switch (event.getState()) {
                case START:
                    // nothing to do
                    break;

                case KEEP_ALIVE:
                    jobService.keepAlive(event.getJobId());
                    break;

                case FAILED:
                    final OffsetDateTime ts = ofInstant(ofEpochMilli(event.getTimestamp()), systemDefault());
                    jobService.appendMessage(event.getJobId(), jobMessage(ERROR, event.getMessage(), ts));
                    break;

                case RESTART:
                    jobService.markRestarted(event.getJobId());
                    break;

                case DEAD:
                    jobService.killJob(event.getJobId());
                    break;

                case SKIPPED:
                    jobService.markSkipped(event.getJobId());
                    jobService.stopJob(event.getJobId());
                    break;

                case STOP:
                    jobService.stopJob(event.getJobId());
                    break;
            }
        }
        catch(RuntimeException e) {
            LOG.error("Failed to persist job state change: jobId="+event.getJobId()+", state="+event.getState()+", message="+event.getMessage(), e);
        }
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {
        try {
            JobMessage jobMessage = convertMessage(messageEvent);
            jobService.appendMessage(messageEvent.getJobId(), jobMessage);
        }
        catch(RuntimeException e) {
            LOG.error("Failed to persist job message (jobId="+messageEvent.getJobId()+"): "+messageEvent.getMessage(), e);
        }
    }

    private JobMessage convertMessage(MessageEvent messageEvent) {
        return jobMessage(messageEvent.getLevel(), messageEvent.getMessage(),
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(messageEvent.getTimestamp()), ZoneId.systemDefault()));
    }

}
