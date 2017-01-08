package de.otto.edison.jobs.status;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static java.lang.String.format;
import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Strategy used to calculate the StatusDetail for the last N executions of a job.
 * <p>
 *     JobStatusCalculators are used to calculate the {@link StatusDetail} for a {@link JobStatusDetailIndicator}
 *     using the last couple of job executions.
 * </p>
 * <p>
 *     Multiple calculators can be configured as a Spring Bean. They are identified by their unique {@link #key} and
 *     configured in the {@code application.properties} for {@link JobDefinition#jobType() job types} as follows:
 * </p>
 * <pre><code>
 *     edison.jobs.status.default=&lt;key of calculator&gt;
 *     edison.jobs.status.&lt;someJobType&gt;=&lt;key of calculator&gt;
 *     edison.jobs.status.&lt;otherJobType&gt;=&lt;key of calculator&gt;
 * </code></pre>
 * <p>
 *     The JobStatusCalculator can be configured to behave differently, depending on
 *     how many jobs failed in the last couple of executions:
 * </p>
 * <ul>
 *     <li>
 *         {@code numberOfJobs}: This specifies how many of the last job executions are taken into the calculation.
 *     </li>
 *     <li>
 *         {@code maxFailedJobs}: The maximum number of jobs that are accepted to fail.
 *     </li>
 * </ul>
 * Depending on the state of the last job execution and the number of failed jobs during the last {@code numberOfJobs},
 * the result of the calculator is as follows:
 * <ul>
 *     <li>
 *         If the last job execution was {@link JobStatus#OK successful}, the calculator will resolve to
 *         {@link Status#WARNING}, if more than {@code maxFailedJobs} out of the last {@code numberOfJobs} have
 *         failed.
 *     </li>
 *     <li>
 *         If the last job execution {@link JobStatus#ERROR failed} for some reason, the calculator will
 *         resolve to {@link Status#ERROR}, if more than {@code maxFailedJobs} out of the last
 *         {@code numberOfJobs} have failed. Otherwise, the result of the calculation will be
 *         {@link Status#WARNING}
 *     </li>
 * </ul>
 * If the last job is {@link JobStatus#DEAD}, the resulting status will be {@link Status#WARNING}.
 */
public class JobStatusCalculator {

    private static final Logger LOG = getLogger(JobStatusCalculator.class);

    private static final String SUCCESS_MESSAGE = "Last job was successful";
    private static final String ERROR_MESSAGE = "Job had an error";
    private static final String DEAD_MESSAGE = "Job died";
    private static final String TOO_MANY_JOBS_FAILED_MESSAGE = "%d out of %d job executions failed";
    private static final String JOB_TOO_OLD_MESSAGE = "Job didn't run in the past %s";
    private static final String LOAD_JOBS_EXCEPTION_MESSAGE = "Failed to load job status";

    private final String key;
    private final int numberOfJobs;
    private final int maxFailedJobs;
    private final JobRepository jobRepository;

    /**
     * Creates a JobStatusCalculator.
     *
     * @param key the key of the calculator.
     * @param numberOfJobs the total number of jobs to take into calculation.
     * @param maxFailedJobs the maximum number of jobs that that are accepted to fail.
     * @param jobRepository repository to fetch the last {@code numberOfJobs}.
     */
    public JobStatusCalculator(final String key,
                               final int numberOfJobs,
                               final int maxFailedJobs,
                               final JobRepository jobRepository) {
        checkArgument(!key.isEmpty(), "Key must not be empty");
        checkArgument(maxFailedJobs <= numberOfJobs, "Parameter maxFailedJobs must not be greater numberOfJobs");
        checkArgument(numberOfJobs > 0, "Parameter numberOfJobs must be greater 0");
        checkArgument(maxFailedJobs >= 0, "Parameter maxFailedJobs must not be negative");
        this.key = key;
        this.numberOfJobs = numberOfJobs;
        this.maxFailedJobs = maxFailedJobs;
        this.jobRepository = jobRepository;
    }

    /**
     * Builds a JobStatusCalculator that is reporting {@link Status#WARNING} if the last job failed.
     *
     * @param key key of the calculator
     * @param jobRepository the repository
     * @return JobStatusCalculator
     */
    public static JobStatusCalculator warningOnLastJobFailed(final String key, final JobRepository jobRepository) {
        return new JobStatusCalculator(
                key, 1, 1, jobRepository
        );
    }

    /**
     * Builds a JobStatusCalculator that is reporting {@link Status#ERROR} if the last job failed.
     *
     * @param key key of the calculator
     * @param jobRepository the repository
     * @return JobStatusCalculator
     */
    public static JobStatusCalculator errorOnLastJobFailed(final String key, final JobRepository jobRepository) {
        return new JobStatusCalculator(
                key, 1, 0, jobRepository
        );
    }

    /**
     * Builds a JobStatusCalculator that is reporting {@link Status#ERROR} if the last {@code numJobs} job failed.
     *
     * @param key key of the calculator
     * @param jobRepository the repository
     * @return JobStatusCalculator
     */
    public static JobStatusCalculator errorOnLastNumJobsFailed(final String key, final int numJobs, final JobRepository jobRepository) {
        return new JobStatusCalculator(
                key, numJobs, numJobs-1, jobRepository
        );
    }

    /**
     * The key of the JobStatusCalculator.
     * <p>
     *     Used as a value of the application property {@code edison.jobs.status.calculator.*} to configure the
     *     calculator for a {@link JobDefinition#jobType() job type}
     * </p>
     *
     * @return key used to select one of several calculators
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns a StatusDetail for a JobDefinition. The Status of the StatusDetail is calculated using
     * the last job executions and depends on the configuration of the calculator.
     *
     * @param jobDefinition definition of the job to calculate.
     * @return StatusDetail of job executions of the {@link JobDefinition#jobType()}
     */
    public StatusDetail statusDetail(final JobDefinition jobDefinition) {
        try {
            final List<JobInfo> jobs = jobRepository.findLatestBy(jobDefinition.jobType(), numberOfJobs);
            return jobs.isEmpty()
                    ? statusDetailWhenNoJobAvailable(jobDefinition)
                    : toStatusDetail(jobs, jobDefinition);
        } catch (final Exception e) {
            LOG.error(LOAD_JOBS_EXCEPTION_MESSAGE + ": " + e.getMessage());
            return StatusDetail.statusDetail(jobDefinition.jobName(), ERROR, LOAD_JOBS_EXCEPTION_MESSAGE);
        }
    }

    private StatusDetail statusDetailWhenNoJobAvailable(final JobDefinition jobDefinition) {
        return StatusDetail.statusDetail(jobDefinition.jobName(), Status.OK, SUCCESS_MESSAGE);
    }

    /**
     * Calculates the StatusDetail from the last job executions.
     *
     * @param jobInfos one or more JobInfo
     * @param jobDefinition definition of the last job
     * @return StatusDetail to indicate for the given last job
     */
    protected StatusDetail toStatusDetail(final List<JobInfo> jobInfos,
                                          final JobDefinition jobDefinition) {
        final Status status;
        final String message;
        final JobInfo lastJob = jobInfos.get(0);
        long numFailedJobs = getNumFailedJobs(jobInfos);
        switch(lastJob.getStatus()) {
            case OK:
                if(jobTooOld(lastJob, jobDefinition)) {
                    status = WARNING;
                    message = jobAgeMessage(jobDefinition);
                } else if (numFailedJobs > maxFailedJobs) {
                    status = WARNING;
                    message = format(TOO_MANY_JOBS_FAILED_MESSAGE, numFailedJobs, jobInfos.size());
                } else {
                    status = OK;
                    message = SUCCESS_MESSAGE;
                }
                break;
            case ERROR:
                if (numFailedJobs > maxFailedJobs) {
                    status = ERROR;
                } else {
                    status = WARNING;
                }
                if (numberOfJobs == 1 && maxFailedJobs <= 1) {
                    message = ERROR_MESSAGE;
                } else {
                    message = format(TOO_MANY_JOBS_FAILED_MESSAGE, numFailedJobs, jobInfos.size());
                }
                break;

            case DEAD:
            default:
                status = WARNING;
                message = DEAD_MESSAGE;
        }
        return StatusDetail.statusDetail(jobDefinition.jobName(), status, message, runningDetailsFor(lastJob));
    }

    /**
     * Returns the number of failed jobs.
     *
     * @param jobInfos list of job infos
     * @return num failed jobs
     */
    protected final long getNumFailedJobs(final List<JobInfo> jobInfos) {
        return jobInfos
                .stream()
                .filter(job -> JobStatus.ERROR.equals(job.getStatus()))
                .count();
    }

    /**
     * Returns additional information like job uri, running state, started and stopped timestamps.
     *
     * @param jobInfo the job information of the last job
     * @return map containing uri, starting, and running or stopped entries.
     */
    protected Map<String, String> runningDetailsFor(final JobInfo jobInfo) {
        Map<String, String> details = new HashMap<>();
        String uri = jobInfo.getJobId();
        details.put("uri", uri);
        if (!jobInfo.getStopped().isPresent()) {
            details.put("running", uri);
        } else {
            details.put("stopped", ISO_DATE_TIME.format(jobInfo.getStopped().get()));
        }
        details.put("started", ISO_DATE_TIME.format(jobInfo.getStarted()));

        return details;
    }

    /**
     * Calculates whether or not the last job execution is too old.
     *
     * @param jobInfo job info of the last job execution
     * @param jobDefinition job definition, specifying the max age of jobs
     * @return boolean
     */
    protected boolean jobTooOld(final JobInfo jobInfo, final JobDefinition jobDefinition) {
        final Optional<OffsetDateTime> stopped = jobInfo.getStopped();
        if (stopped.isPresent() && jobDefinition.maxAge().isPresent()) {
            final OffsetDateTime deadlineToRerun = stopped.get().plus(jobDefinition.maxAge().get());
            return deadlineToRerun.isBefore(now());
        }

        return false;
    }

    private String jobAgeMessage(JobDefinition jobDefinition) {
        return format(JOB_TOO_OLD_MESSAGE, (jobDefinition.maxAge().isPresent() ? jobDefinition.maxAge().get().getSeconds() + " seconds" : "N/A"));
    }

    private void checkArgument(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

}
