package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.repository.JobStateRepository;
import de.otto.edison.jobs.service.JobRunnable;

import java.time.Instant;

import static java.time.Instant.*;

/**
 * Abstract class used to implement stateful jobs.
 * <p>
 *     Provides access to meta information that is persisted between job executions.
 *     Meta data is stored in a {@link JobStateRepository}.
 * </p>
 */
public abstract class StatefulJobRunnable implements JobRunnable {

    /**
     * Helper class used to get and set meta data.
     */
    public final class Meta {
        private final JobStateRepository jobStateRepository;

        Meta(final JobStateRepository jobStateRepository) {
            this.jobStateRepository = jobStateRepository;
        }


        public void delete(final String key) {
            checkKey(key);
            jobStateRepository.setValue(getJobDefinition().jobType(), key, null);
        }

        public void set(final String key, final String value) {
            checkKey(key);
            jobStateRepository.setValue(getJobDefinition().jobType(), key, value);
        }

        public void set(final String key, final int value) {
            checkKey(key);
            jobStateRepository.setValue(getJobDefinition().jobType(), key, String.valueOf(value));
        }

        public void set(final String key, final long value) {
            checkKey(key);
            jobStateRepository.setValue(getJobDefinition().jobType(), key, String.valueOf(value));
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
            final String value = jobStateRepository.getValue(getJobDefinition().jobType(), key);
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

    private final Meta meta;

    protected StatefulJobRunnable(final JobStateRepository jobStateRepository) {
        this.meta = new Meta(jobStateRepository);
    }

    /**
     * Provides access to the meta data of the job type.
     *
     * @return Meta
     */
    protected Meta meta() {
        return meta;
    }

}
