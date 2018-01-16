package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.service.JobRunnable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableFixedDelayJobDefinition;
import static java.time.Duration.ofMinutes;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class BarJob implements JobRunnable {

    private static final Logger LOG = getLogger(BarJob.class);

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
    public boolean execute() {
        if (hasNothingToDo()) {
            return false;
        } else {
            if (hasSomeErrorCondition()) {
                LOG.error(JobMarker.JOB, "Some random error occured");
            }
            for (int i = 0; i < 10; ++i) {
                doSomeHardWork();
            }
            return true;
        }
    }

    private void doSomeHardWork() {
        try {
            LOG.info(JobMarker.JOB, "Still doing some hard work...");
        } catch (final Exception e) {
            LOG.error(JobMarker.JOB, e.getMessage());
        }
    }

    private boolean hasSomeErrorCondition() {
        return new Random().nextBoolean();
    }

    private boolean hasNothingToDo() {
        return new Random().nextBoolean();
    }

}
