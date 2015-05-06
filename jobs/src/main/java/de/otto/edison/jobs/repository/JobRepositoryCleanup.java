package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.configuration.JobConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * A component that is responsible for cleaning up the job repository.
 *
 * All JobCleanup strategies are automatically registered and executed every minute.
 *
 * @author Guido Steinacker
 * @since 01.03.15
 */
public class JobRepositoryCleanup {

    private static final Logger LOG = LoggerFactory.getLogger(JobRepositoryCleanup.class);

    public static final long ONE_MINUTE = 60 * 1000L;

    @Autowired
    private CounterService counterService;
    @Autowired
    private JobRepository repository;

    @Autowired
    private List<JobCleanupStrategy> strategies;

    @Scheduled(fixedDelay = ONE_MINUTE)
    public void cleanup() {
        try {
            for (final JobCleanupStrategy strategy : strategies) {
                strategy.doCleanUp(repository);
            }
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            counterService.increment("counter.jobs.cleanup.errors");
        }
    }
}
