package de.otto.edison.jobs.domain;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Meta information about a job.
 *
 * @since 1.0.0
 */
public final class JobMeta {

    private final String jobType;
    private final boolean running;
    private final boolean disabled;
    private final String disableComment;
    private final Map<String,String> meta;

    public JobMeta(final String jobType,
                   final boolean running,
                   final boolean disabled,
                   final String disableComment,
                   final Map<String,String> meta) {
        this.jobType = jobType;
        this.running = running;
        this.disabled = disabled;
        this.disableComment = disableComment != null ? disableComment : "";
        this.meta = unmodifiableMap(meta);
    }

    public String getJobType() {
        return jobType;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getDisabledComment() {
        return disableComment;
    }

    public String get(final String key) {
        return meta.get(key);
    }

    public Map<String,String> getAll() {
        return meta;
    }
}