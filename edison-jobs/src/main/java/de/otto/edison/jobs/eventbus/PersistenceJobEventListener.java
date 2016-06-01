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
import java.util.Optional;
import java.util.function.Consumer;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;

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
        switch (event.getState()) {
            case START:
                jobRepository.createOrUpdate(newJobInfo(event.getJobId(), event.getJobType(), clock,
                        systemInfo.getHostname()));
                break;

            case KEEP_ALIVE:
                updateJobIfPresent(event.getJobId(), JobInfo::ping);
                break;

            case RESTART:
                updateJobIfPresent(event.getJobId(), JobInfo::restart);
                break;

            case DEAD:
                updateJobIfPresent(event.getJobId(), JobInfo::dead);
                break;

            case STOP:
                updateJobIfPresent(event.getJobId(), JobInfo::stop);
                break;
        }
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {
        JobMessage jobMessage = JobMessage.jobMessage(convertLevel(messageEvent), messageEvent.getMessage());

        switch (messageEvent.getLevel()) {
            case ERROR:
                updateJobIfPresent(messageEvent.getJobId(), jobInfo -> jobInfo.error(messageEvent.getMessage()));
                break;
            default:
                jobRepository.appendMessage(messageEvent.getJobId(), jobMessage);
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

    private void updateJobIfPresent(final String jobId, final Consumer<JobInfo> consumer) {
        final Optional<JobInfo> jobInfo = jobRepository.findOne(jobId);
        if (jobInfo.isPresent()) {
            consumer.accept(jobInfo.get());
            jobRepository.createOrUpdate(jobInfo.get());
        } else {
            LOG.error("job '{}' in inconsistent state. Should be present here", jobId);
        }
    }
}
