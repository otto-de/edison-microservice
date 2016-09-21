package de.otto.edison.mongo;

/**
 * Result of the update-if-match operation.
 */
public enum UpdateIfMatchResult {

    /**
     * Everything is fine while updating the document.
     */
    OK,

    /**
     * Document which has to be updated not found.
     */
    NOT_FOUND,

    /**
     * Document was concurrently modified by an other client.
     */
    CONCURRENTLY_MODIFIED;
}
