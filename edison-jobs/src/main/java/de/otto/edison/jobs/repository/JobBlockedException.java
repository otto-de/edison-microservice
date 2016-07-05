package de.otto.edison.jobs.repository;

public class JobBlockedException extends RuntimeException {

    public JobBlockedException(String message) {
        super(message);
    }
}
