package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMarker;
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
public class FooJob implements JobRunnable {

    private static final Logger LOG = getLogger(FooJob.class);

    @Override
    public JobDefinition getJobDefinition() {
        return fixedDelayJobDefinition(
                "Foo",
                "Foo Job",
                "An example job that is running for a while.",
                ofHours(1),
                0,
                Optional.of(ofHours(3))
        );
    }

    @Override
    public boolean execute() {
        for (int i = 0; i < 60; ++i) {
            doSomeHardWork();
        }
        return true;
    }

    private void doSomeHardWork() {
        try {
            LOG.info(JobMarker.JOB, "Still doing some hard work...");
            sleep(new Random(42).nextInt(2000));
        } catch (final InterruptedException e) {
            LOG.error(JobMarker.JOB, e.getMessage());
        }
    }
}
