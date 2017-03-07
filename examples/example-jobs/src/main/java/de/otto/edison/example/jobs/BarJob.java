package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableFixedDelayJobDefinition;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.SKIPPED;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofMinutes;

@Component
public class BarJob implements JobRunnable {

    @Override
    public JobDefinition getJobDefinition() {
        return retryableFixedDelayJobDefinition(
                "Bar",
                "Bar Job",
                "An example job that is running for a while and skip and has a long long long long long long long long long long long long long long long long long long long long description.",
                ofMinutes(2),
                1,
                3,
                Optional.of(ofMinutes(2)),
                Optional.of(ofMinutes(20))
        );
    }

    @Override
    public void execute(final JobEventPublisher jobEventPublisher) {
        if (hasNothingToDo()) {
            jobEventPublisher.skipped();
        } else {
            if (hasSomeErrorCondition()) {
                jobEventPublisher.error("Some random error occured");
            }
            for (int i = 0; i < 10; ++i) {
                doSomeHardWork(jobEventPublisher);
            }
        }
    }

    private void doSomeHardWork(final JobEventPublisher jobEventPublisher) {
        try {
            jobEventPublisher.info("Still doing some hard work...");
            jobEventPublisher.skipped();
        } catch (final InterruptedException e) {
            jobEventPublisher.error(e.getMessage());
        }
    }

    private boolean hasSomeErrorCondition() {
        return new Random().nextBoolean();
    }

    private boolean hasNothingToDo() {
        return new Random().nextBoolean();
    }

}
