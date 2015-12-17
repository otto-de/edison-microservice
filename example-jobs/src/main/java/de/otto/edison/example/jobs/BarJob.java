package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableFixedDelayJobDefinition;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.ERROR;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.INFO;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofMinutes;

@Component
public class BarJob implements JobRunnable {

    @Override
    public JobDefinition getJobDefinition() {
        return retryableFixedDelayJobDefinition(
                "Bar",
                "Bar Job",
                "An example job that is running for a while and has a long long long long long long long long long long long long long long long long long long long long description.",
                ofMinutes(2),
                1,
                3,
                Optional.of(ofMinutes(2)),
                Optional.of(ofMinutes(20))
        );
    }

    @Override
    public void execute(final JobInfo jobInfo, final JobEventPublisher jobEventPublisher) {
        if (new Random().nextBoolean()) {
            jobEventPublisher.message(ERROR, "Some random error occured");
            jobInfo.error("Some random error occured.");
        }
        for (int i = 0; i < 10; ++i) {
            doSomeHardWork(jobInfo, jobEventPublisher);
        }
    }

    private void doSomeHardWork(final JobInfo jobInfo, final JobEventPublisher jobEventPublisher) {
        try {
            jobEventPublisher.message(INFO, "Still doing some hard work...");
            jobInfo.info("Still doing some hard work...");
            sleep(new Random(42).nextInt(2000));
        } catch (final InterruptedException e) {
            jobInfo.error(e.getMessage());
        }
    }

}
