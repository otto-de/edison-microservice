package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofHours;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
@Component
public class FooJob implements JobRunnable {

    public FooJob() {
    }

    @Override
    public JobDefinition getJobDefinition() {
        return fixedDelayJobDefinition(
                "Foo",
                "Foo Job",
                "An example job that is running for a while.",
                ofHours(1),
                Optional.of(ofHours(3))
        );
    }

    @Override
    public void execute(final JobInfo jobInfo) {
        for (int i = 0; i < 10; ++i) {
            doSomeHardWork(jobInfo);
        }
    }

    private void doSomeHardWork(final JobInfo jobInfo) {
        try {
            jobInfo.info("Still doing some hard work...");
            sleep(new Random(42).nextInt(2000));
        } catch (final InterruptedException e) {
            jobInfo.error(e.getMessage());
        }
    }

}
