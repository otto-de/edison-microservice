package de.otto.edison.jobs.eventbus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventbusTestConfiguration {

    @Bean
    public InMemoryEventRubbishBin testInMemoryEventListener() {
        return new InMemoryEventRubbishBin();
    }
}
