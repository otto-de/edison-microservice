package de.otto.edison.dynamodb.jobs;

import de.otto.edison.annotations.Beta;

@Beta
enum JobStructure {

    ID("_id"),
    STARTED("started"),
    STOPPED("stopped"),
    JOB_TYPE("type"),
    STATUS("status"),
    MESSAGES("messages"),
    MSG_TS("ts"),
    MSG_TEXT("msg"),
    MSG_LEVEL("level"),
    HOSTNAME("hostname"),
    LAST_UPDATED("lastUpdated");

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
