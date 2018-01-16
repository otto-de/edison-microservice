package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.service.JobRunnable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableFixedDelayJobDefinition;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofMinutes;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FizzleJob implements JobRunnable {

    private static final Logger LOG = getLogger(FizzleJob.class);

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
    public boolean execute() {
        if (new Random().nextBoolean()) {
            LOG.error(JobMarker.JOB, "Some random error occured");
        }
        for (int i = 0; i < 10; ++i) {
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
