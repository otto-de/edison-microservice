package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClearDeadLocks {

    private static final Logger LOG = LoggerFactory.getLogger(ClearDeadLocks.class);

    public static final long FIVE_MINUTES = 5L * 60L * 1000L;

    private final JobRepository jobRepository;

    @Autowired
    public ClearDeadLocks(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedRate = FIVE_MINUTES)
    public void clearLocks() {
        jobRepository.runningJobsDocument()
                .getRunningJobs()
                .stream()
                .map(rj -> jobRepository.findOne(rj.jobId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(JobInfo::isStopped)
                .forEach(stoppedJob -> {
                    jobRepository.clearRunningMark(stoppedJob.getJobType());
                    LOG.info("Clear Lock of Job {}", stoppedJob.getJobType());
                });
    }
}
