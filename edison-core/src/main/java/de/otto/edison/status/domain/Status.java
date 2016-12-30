package de.otto.edison.status.domain;

public enum Status {

    OK, WARNING, ERROR;

    public static Status plus(final Status first, final Status second) {
        if (first == ERROR || second == ERROR) return ERROR;
        if (first == WARNING || second == WARNING) return WARNING;
        return first;
    }
}
