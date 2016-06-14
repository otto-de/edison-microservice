package de.otto.edison.jobs.repository.mongo;

enum JobRunLockStructure {

    ID("_id"),
    CREATED("created");

    private final String key;

    private JobRunLockStructure(final String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String toString() {
        return key;
    }

}
