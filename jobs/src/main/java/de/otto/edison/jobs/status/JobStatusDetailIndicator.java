package de.otto.edison.jobs.status;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static java.time.OffsetDateTime.now;


public class JobStatusDetailIndicator implements StatusDetailIndicator {
    public static final String SUCCESS_MESSAGE = "Last job was successful";
    public static final String ERROR_MESSAGE = "Job had an error";
    public static final String JOB_TOO_OLD_MESSAGE = "Job didn't run in the past ";

    private final JobRepository jobRepository;
    private final String name;
    private final String jobType;
    private final String baseUrl;
    private final Duration maxAge;

    public JobStatusDetailIndicator(final JobRepository jobRepository,
                                    final String name,
                                    final String jobType,
                                    final String baseUrl,
                                    final Duration maxAge) {
        this.jobRepository = jobRepository;
        this.name = name;
        this.jobType = jobType;
        this.baseUrl = baseUrl;
        this.maxAge = maxAge;
    }

    @Override
    public StatusDetail statusDetail() {
        List<JobInfo> jobs = jobRepository.findLatestBy(jobType, 1);

        return jobs.isEmpty() ? statusDetailWhenNoJobAvailable() : toStatusDetail(jobs.get(0));
    }

    private StatusDetail toStatusDetail(JobInfo jobInfo) {
        Status status;
        String message;

        if (JobInfo.JobStatus.OK.equals(jobInfo.getStatus()) && jobTooOld(jobInfo)) {
            status =  WARNING;
            message = JOB_TOO_OLD_MESSAGE + maxAge;
        }else if (JobInfo.JobStatus.OK.equals(jobInfo.getStatus())) {
            status =  OK;
            message = SUCCESS_MESSAGE;
        } else {
            status = WARNING;
            message = ERROR_MESSAGE;
        }
        return StatusDetail.statusDetail(name, status, message, runningDetailsFor(jobInfo));
    }

    private StatusDetail statusDetailWhenNoJobAvailable() {
        return StatusDetail.statusDetail(name, Status.OK, SUCCESS_MESSAGE);
    }

    private Map<String, String> runningDetailsFor(JobInfo jobInfo) {
        Map<String, String> details = new HashMap<>();
        String uri = baseUrl + jobInfo.getJobUri().toString();
        details.put("uri", uri);
        if (!jobInfo.getStopped().isPresent()) {
            details.put("running", uri);
        }

        return details;
    }

    private boolean jobTooOld(JobInfo jobInfo) {
        Optional<OffsetDateTime> stopped = jobInfo.getStopped();
        if (stopped.isPresent()) {
            OffsetDateTime deadlineToRerun = stopped.get().plus(maxAge);
            return deadlineToRerun.isBefore(now());
        }

        return false;
    }
}
