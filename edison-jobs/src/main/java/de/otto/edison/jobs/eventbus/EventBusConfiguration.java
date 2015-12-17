package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Clock.systemDefaultZone;

@Configuration
public class EventBusConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Bean
    public JobEventListener logJobEventListener() {
        return new LogJobEventListener();
    }

    @Bean
    public JobEventListener persistenceJobEventListener() {
        return new PersistenceJobEventListener(jobRepository, systemDefaultZone());
    }
}
