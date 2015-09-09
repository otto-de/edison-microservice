package de.otto.edison.example.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.stereotype.Component;

import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
@Component
public class FooJob implements JobRunnable {

    public FooJob() {
    }

    public String getJobType() {
        return "Foo";
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
