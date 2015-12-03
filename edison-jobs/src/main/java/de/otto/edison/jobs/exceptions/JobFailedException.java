package de.otto.edison.jobs.exceptions;

public class JobFailedException extends RuntimeException {

    public JobFailedException() {
    }

    public JobFailedException(String message) {
        super(message);
    }

    public JobFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobFailedException(Throwable cause) {
        super(cause);
    }

    public JobFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
