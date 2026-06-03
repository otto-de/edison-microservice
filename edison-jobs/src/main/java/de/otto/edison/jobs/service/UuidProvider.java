package de.otto.edison.jobs.service;

import java.util.UUID;

public class UuidProvider {

    public String getUuid() {
        return UUID.randomUUID().toString();
    }


}
