package de.otto.edison.jobs.service;

import java.time.OffsetDateTime;

public class Clock {
    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
