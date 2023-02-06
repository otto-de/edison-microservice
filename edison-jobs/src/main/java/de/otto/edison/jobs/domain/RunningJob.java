package de.otto.edison.jobs.domain;

import java.util.Objects;

/**
 * A currently running job.
 *
 * @since 1.0.0
 */
public record RunningJob(
        String jobId,
        String jobType
) {}
