package de.otto.edison.jobs.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidProvider {

    public String getUuid() {
        return UUID.randomUUID().toString();
    }


}
