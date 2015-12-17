package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventbusTestConfiguration {

    @Bean
    public InMemoryEventRubbishBin testInMemoryEventListener() {
        return new InMemoryEventRubbishBin();
    }

    @Bean
    public JobRepository jobRepository() {
        return new InMemJobRepository();
    }
}
