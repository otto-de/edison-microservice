package de.otto.edison.jobs.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidProvider {

    public String getUuid() {
        return UUID.randomUUID().toString();
    }


}
