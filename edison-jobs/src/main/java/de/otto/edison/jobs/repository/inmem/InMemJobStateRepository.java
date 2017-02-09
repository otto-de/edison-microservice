package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.repository.JobStateRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemJobStateRepository implements JobStateRepository {

    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();

    @Override
    public String setValue(String jobType, String key, String value) {
        map.putIfAbsent(jobType, new ConcurrentHashMap<>());
        if (value != null) {
            return map.get(jobType).put(key, value);
        } else {
            return map.get(jobType).remove(key);
        }
    }

    @Override
    public String getValue(String jobType, String key) {
        return map.getOrDefault(jobType, Collections.emptyMap()).get(key);
    }

    /**
     * Returns all job types matching the specified predicate.
     *
     * @return set containing matching job types.
     */
    @Override
    public Set<String> findAllJobTypes() {
        return map.keySet();
    }

    /**
     * Deletes all information from the repository.
     */
    @Override
    public void deleteAll() {
        map.clear();
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        if (getValue(jobType, key) == null) {
            setValue(jobType, key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "InMemJobStateRepository";
    }

}
