package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofHours;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
@Component
public class OldJob implements JobRunnable {

    private static final Logger LOG = getLogger(OldJob.class);

    @Override
    public JobDefinition getJobDefinition() {
        return fixedDelayJobDefinition(
                "Old",
                "Old Job",
                "An example job with JobEventPublisher that is running for a while.",
                ofHours(1),
                0,
                Optional.of(ofHours(3))
        );
    }

    @Override
    public boolean execute(final JobEventPublisher jobEventPublisher) {
        for (int i = 0; i < 10; ++i) {
            doSomeWork(jobEventPublisher);
        }
        jobEventPublisher.skipped();
        return true;
    }

    private void doSomeWork(final JobEventPublisher jobEventPublisher) {
        try {
            jobEventPublisher.info("Still doing some work...");
            sleep(new Random(42).nextInt(100));
        } catch (final InterruptedException e) {
            LOG.error(JobMarker.JOB, e.getMessage());
        }
    }
}
