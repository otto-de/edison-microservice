package de.otto.edison.jobs.repository;

import java.util.Set;

/**
 * Repository used to associate meta data with job types.
 * <p>
 *     Some jobs may need to keep state between multiple job executions. For example, it may be necessary to store
 *     the position of the last successful data import, so follow-up executions of a job is able to start from
 *     where the last execution has finished.
 * </p>
 */
public interface JobStateRepository {

    /**
     * Create property if the document or key does not exists.
     *
     * @param jobType the job type
     * @param key the key of the created property
     * @param value property value
     * @return true if value was created, false if value already exists
     */
    boolean createValue(String jobType, String key, String value);

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
     * Retruns the value of the specified property for a job type, or null if the property does not exist.
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
