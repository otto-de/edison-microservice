package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public class InMemJobMetaRepository implements JobMetaRepository {

    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";

    @Override
    public String getRunningJob(final String jobType) {
        return getValue(jobType, KEY_RUNNING);
    }

    @Override
    public boolean setRunningJob(final String jobType, final String jobId) {
        return createValue(jobType, KEY_RUNNING, jobId);
    }

    /**
     * Clears the job running mark of the jobType. Does nothing if not mark exists.
     *
     * @param jobType the job type
     */
    @Override
    public void clearRunningJob(final String jobType) {
        setValue(jobType, KEY_RUNNING, null);
    }

    /**
     * Reenables a job type that was disabled
     *
     * @param jobType the enabled job type
     */
    @Override
    public void enable(final String jobType) {
        setValue(jobType, KEY_DISABLED, null);
    }

    /**
     * Disables a job type, i.e. prevents it from being started
     *
     * @param jobType the disabled job type
     * @param comment an optional comment
     */
    @Override
    public void disable(final String jobType, final String comment) {
        setValue(jobType, KEY_DISABLED, comment != null ? comment : "");
    }

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

    /**
     * Returns the current state of the specified job type.
     *
     * @param jobType the job type
     * @return current state of the job type
     */
    @Override
    public JobMeta getJobMeta(String jobType) {
        final Map<String, String> document = map.get(jobType);
        if (document != null) {
            final Map<String, String> meta = document.keySet()
                    .stream()
                    .filter(key -> !key.startsWith("_e_"))
                    .collect(toMap(
                            key -> key,
                            document::get
                    ));
            final boolean isRunning = document.containsKey(KEY_RUNNING);
            final boolean isDisabled = document.containsKey(KEY_DISABLED);
            final String comment = document.get(KEY_DISABLED);
            return new JobMeta(jobType, isRunning, isDisabled, comment, meta);
        } else {
            return new JobMeta(jobType, false, false, "", emptyMap());
        }
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
        return "InMemJobMetaRepository";
    }

}
