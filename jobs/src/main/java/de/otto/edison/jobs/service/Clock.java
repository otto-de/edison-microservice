package de.otto.edison.jobs.service;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class Clock {
    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
