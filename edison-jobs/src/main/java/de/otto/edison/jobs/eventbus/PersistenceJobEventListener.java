package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.repository.JobRepository;

import java.time.Clock;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;

public class PersistenceJobEventListener implements JobEventListener {

    private final JobRepository jobRepository;
    private final Clock clock;

    public PersistenceJobEventListener(final JobRepository jobRepository, final Clock clock) {
        this.jobRepository = jobRepository;
        this.clock = clock;
    }

    @Override
    public void consumeStateChange(final StateChangeEvent event) {
        switch (event.getState()) {

            case CREATE:
                jobRepository.createOrUpdate(newJobInfo(event.getJobUri(), event.getJobType(), null, clock));
                break;

            case STILL_ALIVE:
                Optional<JobInfo> jobInfo = jobRepository.findOne(event.getJobUri());
                if (jobInfo.isPresent()) {
                    jobInfo.get().ping();
                    jobRepository.createOrUpdate(jobInfo.get());
                }
                break;
        }
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {
    }
}
