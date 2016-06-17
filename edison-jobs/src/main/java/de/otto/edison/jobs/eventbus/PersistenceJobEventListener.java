package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.time.OffsetDateTime.now;

public class PersistenceJobEventListener implements JobEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceJobEventListener.class);

    private final JobRepository jobRepository;
    private final Clock clock;
    private final SystemInfo systemInfo;

    public PersistenceJobEventListener(final JobRepository jobRepository, final Clock clock,
                                       final SystemInfo systemInfo) {
        this.jobRepository = jobRepository;
        this.clock = clock;
        this.systemInfo = systemInfo;
    }

    @Override
    public void consumeStateChange(final StateChangeEvent event) {
        if (event.getState() == StateChangeEvent.State.START) {
            jobRepository.createOrUpdate(newJobInfo(event.getJobId(), event.getJobType(), clock,
                    systemInfo.getHostname()));
            return;
        }
        final Optional<JobInfo> optionalJobInfo = jobRepository.findOne(event.getJobId());
        if (!optionalJobInfo.isPresent()) {
            LOG.error("job '{}' in inconsistent state. Should be present here", event.getJobId());
            return;
        }
        JobInfo jobInfo = optionalJobInfo.get();
        OffsetDateTime time = now(jobInfo.getClock());

        switch (event.getState()) {
            case KEEP_ALIVE:
                jobRepository.createOrUpdate(jobInfo.copy()
                        .setLastUpdated(time)
                        .build());
                break;

            case RESTART:
                jobRepository.appendMessage(event.getJobId(), jobMessage(Level.WARNING, "Restarting job ..", time));
                jobRepository.createOrUpdate(jobInfo.copy()
                        .setLastUpdated(time)
                        .setStatus(JobInfo.JobStatus.OK)
                        .build());
                break;

            case DEAD:
                jobRepository.createOrUpdate(
                        jobInfo.copy()
                                .setLastUpdated(time)
                                .setStatus(JobInfo.JobStatus.DEAD)
                                .setStopped(time)
                                .build()
                );
                jobRepository.appendMessage(event.getJobId(), jobMessage(Level.WARNING, "Job didn't receive updates for a while, considering it dead", time));
                break;

            case STOP:
                jobRepository.createOrUpdate(
                        jobInfo.copy()
                                .setLastUpdated(time)
                                .setStopped(time)
                                .build()
                );
                break;
        }
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {

        final Optional<JobInfo> optionalJobInfo = jobRepository.findOne(messageEvent.getJobId());
        if (!optionalJobInfo.isPresent()) {
            LOG.error("job '{}' in inconsistent state. Should be present here", messageEvent.getJobId());
            return;
        }
        JobMessage jobMessage = jobMessage(convertLevel(messageEvent), messageEvent.getMessage(), now(optionalJobInfo.get().getClock()));
        JobInfo jobInfo = optionalJobInfo.get();
        jobRepository.appendMessage(messageEvent.getJobId(), jobMessage);
        if (messageEvent.getLevel() == MessageEvent.Level.ERROR) {
            jobRepository.createOrUpdate(
                    jobInfo.copy()
                            .setStatus(ERROR)
                            .build());
        }
    }

    private Level convertLevel(MessageEvent messageEvent) {
        Level level = Level.INFO;
        switch (messageEvent.getLevel()) {
            case INFO:
                level = Level.INFO;
                break;
            case WARN:
                level = Level.WARNING;
                break;
            case ERROR:
                level = Level.ERROR;
                break;
        }
        return level;
    }
}
