package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.repository.JobMetaRepository;

import java.time.Instant;

import static java.time.Instant.ofEpochMilli;

/**
 * State information about a job.
 * <p>
 *     Every change to the job state is immediately persisted using atomic operations.
 * </p>
 *
 * @since 1.0.0
 */
public final class JobMeta {
    
    private final String jobType;
    private final JobMetaRepository jobMetaRepository;

    public JobMeta(final String jobType,
                   final JobMetaRepository jobMetaRepository) {
        this.jobType = jobType;
        this.jobMetaRepository = jobMetaRepository;
    }

    public String getJobType() {
        return jobType;
    }

    public boolean isDisabled() {
        return jobMetaRepository.getValue(jobType, "_e_disabled") != null;
    }

    public String getDisabledComment() {
        final String disabled = jobMetaRepository.getValue(jobType, "_e_disabled");
        return disabled != null ? disabled : "";
    }

    public void delete(final String key) {
        checkKey(key);
        jobMetaRepository.setValue(jobType, key, null);
    }

    public void set(final String key, final String value) {
        checkKey(key);
        jobMetaRepository.setValue(jobType, key, value);
    }

    public void set(final String key, final int value) {
        checkKey(key);
        jobMetaRepository.setValue(jobType, key, String.valueOf(value));
    }

    public void set(final String key, final long value) {
        checkKey(key);
        jobMetaRepository.setValue(jobType, key, String.valueOf(value));
    }

    public void set(final String key, final Instant value) {
        checkKey(key);
        set(key, value.toEpochMilli());
    }

    public String getAsString(final String key) {
        checkKey(key);
        return getAsString(key, null);
    }

    public String getAsString(final String key, final String defaultValue) {
        checkKey(key);
        final String value = jobMetaRepository.getValue(jobType, key);
        return value != null ? value : defaultValue;
    }

    public int getAsInt(final String key, final int defaultValue) {
        checkKey(key);
        final String value = getAsString(key);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }

    public long getPropertyAsLong(final String key, final long defaultValue) {
        checkKey(key);
        final String value = getAsString(key);
        return value != null ? Long.valueOf(value) : defaultValue;
    }

    public Instant getAsInstant(final String key) {
        checkKey(key);
        return getAsInstant(key, null);
    }

    public Instant getAsInstant(final String key, final Instant defaultValue) {
        checkKey(key);
        final long epochMilli = getPropertyAsLong(key, -1);
        if (epochMilli != -1) {
            return ofEpochMilli(epochMilli);
        } else {
            return defaultValue;
        }
    }

    private void checkKey(final String key) {
        if (key == null || key.startsWith("_e_")) {
            throw new IllegalArgumentException("Keys must never be null and must not start with prefix _e_");
        }
    }
}