package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.EventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.INFO;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofHours;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
@Component
public class FooJob implements JobRunnable {

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
    public void execute(final JobInfo jobInfo, final EventPublisher eventPublisher) {
        for (int i = 0; i < 10; ++i) {
            doSomeHardWork(jobInfo, eventPublisher);
        }
    }

    private void doSomeHardWork(final JobInfo jobInfo, final EventPublisher eventPublisher) {
        try {
            eventPublisher.message(INFO, "Still doing some hard work...");
            jobInfo.info("Still doing some hard work...");
            sleep(new Random(42).nextInt(2000));
        } catch (final InterruptedException e) {
            jobInfo.error(e.getMessage());
        }
    }

}
