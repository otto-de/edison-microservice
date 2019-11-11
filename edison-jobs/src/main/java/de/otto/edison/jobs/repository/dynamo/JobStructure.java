package de.otto.edison.jobs.repository.dynamo;

enum JobStructure {

    ID("jobId"),
    STARTED("started"),
    STOPPED("stopped"),
    JOB_TYPE("type"),
    STATUS("status"),
    MESSAGES("messages"),
    MSG_TS("ts"),
    MSG_TEXT("msg"),
    MSG_LEVEL("level"),
    HOSTNAME("hostname"),
    LAST_UPDATED("lastUpdated"),
    LAST_UPDATED_EPOCH("lastUpdatedEpoch");

    private final String key;

    JobStructure(final String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String toString() {
        return key;
    }

}
