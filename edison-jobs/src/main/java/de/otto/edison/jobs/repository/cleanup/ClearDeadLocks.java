package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClearDeadLocks {

    private static final Logger LOG = LoggerFactory.getLogger(ClearDeadLocks.class);

    public static final long FIVE_MINUTES = 5L * 60L * 1000L;

    private final JobLockRepository jobLockRepository;
    private final JobRepository jobRepository;

    @Autowired
    public ClearDeadLocks(final JobLockRepository jobLockRepository,
                          final JobRepository jobRepository) {
        this.jobLockRepository = jobLockRepository;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedRate = FIVE_MINUTES)
    public void clearLocks() {
        List<RunningJobs.RunningJob> runningJobs = jobLockRepository.runningJobs()
                .getRunningJobs();

        for (RunningJobs.RunningJob runningJob : runningJobs) {
            Optional<JobInfo> jobInfoOptional = jobRepository.findOne(runningJob.jobId);
            if (jobInfoOptional.isPresent() && jobInfoOptional.get().isStopped()) {
                jobLockRepository.clearRunningMark(runningJob.jobType);
                LOG.info("Clear Lock of Job {}. Job stopped already.", runningJob.jobType);
            } else if (!jobInfoOptional.isPresent()){
                jobLockRepository.clearRunningMark(runningJob.jobType);
                LOG.info("Clear Lock of Job {}. JobID does not exist", runningJob.jobType);
            }
        }
    }
}
