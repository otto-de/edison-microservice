package de.otto.µservice.example.jobs;

import de.otto.µservice.jobs.domain.JobType;
import de.otto.µservice.jobs.service.JobRunnable;
import de.otto.µservice.jobs.service.JobRunner;
import org.slf4j.Logger;

import static java.lang.Thread.sleep;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public class FooJob implements JobRunnable {

    // We are using the JobRunner to log the job. logback.xml contains a separate appender associated with JobRunner,
    // that is logging every message with job_id and job_type using MDC
    private static final Logger JOB_LOGGER = getLogger(JobRunner.class);

    private FooJob() {
    }

    public static FooJob fooJob() {
        return new FooJob();
    }

    @Override
    public JobType getJobType() {
        return ExampleJobs.FOO;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; ++i) {
            JOB_LOGGER.info("Still doing some hard work...");
            doSomeHardWork();
        }
    }

    private void doSomeHardWork() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
        /* ignore */
        }
    }

}
