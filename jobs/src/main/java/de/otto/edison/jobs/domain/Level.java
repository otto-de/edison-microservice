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

    public static Level ofKey(final String s) {
        for (Level l : Level.values()) {
            if (l.getKey().equalsIgnoreCase(s)) {
                return l;
            }
        }
        throw new IllegalArgumentException("no level with this key found");
    }

    public String getKey() {
        return key;
    }
}
