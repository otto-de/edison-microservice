package de.otto.edison.jobs.repository.dynamo;

import org.testcontainers.containers.GenericContainer;

public class DynamoContainer extends GenericContainer<DynamoContainer> {

    public DynamoContainer(final String dockerImageName) {
        super(dockerImageName);
    }

}
