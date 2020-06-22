package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class CleanupMessagesOfTooBigJobLogs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupMessagesOfTooBigJobLogs.class);
    private static final long FIND_TOO_LONG_LOGS_INTERVAL = 1L * 60L * 1000L;

    private final JobService jobService;

    public CleanupMessagesOfTooBigJobLogs(final JobService jobService) {
        this.jobService = jobService;
        LOG.info("Init CleanupMessagesOfTooBigJobLogs JobCleanupStrategy.");
    }

    @Override
    @Scheduled(fixedRate = FIND_TOO_LONG_LOGS_INTERVAL)
    public void doCleanUp() {
        jobService.handleTooBigJobLogs();
    }
}
