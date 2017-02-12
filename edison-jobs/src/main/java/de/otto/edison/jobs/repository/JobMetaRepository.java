package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobMeta;

import java.util.Set;

/**
 * Repository used to associate meta data with job types.
 * <p>
 *     Some jobs may need to keep state between multiple job executions. For example, it may be necessary to store
 *     the position of the last successful data import, so follow-up executions of a job is able to start from
 *     where the last execution has finished.
 * </p>
 *
 * @since 1.0.0
 */
public interface JobMetaRepository {

    /**
     * Returns the current state of the specified job type.
     *
     * @param jobType the job type
     * @return current state of the job type
     */
    JobMeta getJobMeta(String jobType);

    /**
     * Create property if the document or key does not exists.
     * <p>
     *     Creating a value is an atomic operation that either succeeds, or fails by returning {@code false}. If
     *     creation fails, the job's state does not change.
     * </p>
     *
     * @param jobType the job type
     * @param key the key of the created property
     * @param value property value
     * @return true if value was created, false if value already exists
     */
    boolean createValue(String jobType, String key, String value);

    boolean setRunningJob(String jobType, String jobId);

    String getRunningJob(String jobType);

    void clearRunningJob(String jobType);

    /**
     * Disables a job type, i.e. prevents it from being started
     *
     * @param jobType the disabled job type
     * @param comment an optional comment
     */
    void disable(String jobType, String comment);

    /**
     * Reenables a job type that was disabled
     *
     * @param jobType the enabled job type
     */
    void enable(String jobType);

    /**
     * Atomically sets or updates a property for a job type.
     *
     * @param jobType the job type
     * @param key the key of the property
     * @param value the (new) value of the property
     * @return previous value of the updated property, or null if it was created.
     */
    String setValue(String jobType, String key, String value);

    /**
     * Returns the value of the specified property for a job type, or null if the property does not exist.
     *
     * @param jobType the job type
     * @param key the key of the property
     * @return current value or null
     */
    String getValue(String jobType, String key);

    /**
     * Returns all job types matching the specified predicate.
     *
     * @return set containing matching job types.
     */
    Set<String> findAllJobTypes();

    /**
     * Deletes all information from the repository.
     */
    void deleteAll();
}
