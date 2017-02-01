package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.repository.JobStateRepository;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class StatefulJob implements JobRunnable {

    @Autowired
    private JobStateRepository jobStateRepository;

    public void setMeta(String key, String value) {
        jobStateRepository.setValue(getJobDefinition().jobType(), key, value);
    }

    public void setMeta(String key, int value) {
        jobStateRepository.setValue(getJobDefinition().jobType(), key, String.valueOf(value));
    }

    public String getMetaAsString(String key) {
        return getMetaAsString(key, null);
    }

    public String getMetaAsString(String key, String defaultValue) {
        final String value = jobStateRepository.getValue(getJobDefinition().jobType(), key);
        return value != null ? value : defaultValue;
    }

    public int getMetaAsInt(String key, int defaultValue) {
        final String value = getMetaAsString(key);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }

}
