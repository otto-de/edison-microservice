package de.otto.edison.jobs.domain;

/**
 * @author Guido Steinacker
 * @since 23.02.15
 */
public enum Level {

    INFO("info"),
    WARNING("warning");

    private final String key;

    Level(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
