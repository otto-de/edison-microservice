package de.otto.edison.jobs.repository;

public interface JobStateRepository {

    void setValue(String jobType, String key, String value);

    String getValue(String jobType, String key);
}
