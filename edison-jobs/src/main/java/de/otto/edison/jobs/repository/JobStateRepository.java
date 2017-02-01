package de.otto.edison.jobs.repository;

import java.time.Instant;

public interface JobStateRepository {

    void setValue(String jobType, String key, String value);

    String getValue(String jobType, String key);
}
