package de.otto.edison.jobs.repository;

public interface JobStateRepository {

    /**
     * create value if not exists
     *
     * @param jobType
     * @param key
     * @param value
     * @return true if value was created, false if value already exists
     */
    boolean createValue(String jobType, String key, String value);

    /**
     * set/update value
     *
     * @param jobType
     * @param key
     * @param value
     */
    void setValue(String jobType, String key, String value);

    /**
     * get the value
     *
     * @param jobType
     * @param key
     * @return null if value does not exists otherwise the value
     */
    String getValue(String jobType, String key);

    void deleteAll();
}
