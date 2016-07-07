package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableFixedDelayJobDefinition;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofMinutes;

@Component
public class FizzleJob implements JobRunnable {

    @Override
    public JobDefinition getJobDefinition() {
        return retryableFixedDelayJobDefinition(
                "Fizzle",
                "Fizzle Job",
                "Mutual Exclusion with BarJob: Lorizzle ipsum dolizzle sit amizzle, consectetuer adipiscing hizzle. Nullizzle sapizzle velizzle, mah nizzle volutpizzle, suscipizzle fo shizzle, gravida vizzle, my shizz. Pellentesque bling bling tortizzle. Sed own yo'. Fo shizzle izzle ghetto fo shizzle mah nizzle fo rizzle, mah home g-dizzle turpizzle tempizzle fo.",
                ofMinutes(2),
                1,
                3,
                Optional.of(ofMinutes(2)),
                Optional.of(ofMinutes(20))
        );
    }

    @Override
    public void execute(final JobEventPublisher jobEventPublisher) {
        if (new Random().nextBoolean()) {
            jobEventPublisher.error("Some random error occured");
        }
        for (int i = 0; i < 10; ++i) {
            doSomeHardWork(jobEventPublisher);
        }
    }

    private void doSomeHardWork(final JobEventPublisher jobEventPublisher) {
        try {
            jobEventPublisher.info("Still doing some hard work...");
            sleep(new Random(42).nextInt(2000));
        } catch (final InterruptedException e) {
            jobEventPublisher.error(e.getMessage());
        }
    }

}
