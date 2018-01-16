package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfiguration {

    @Autowired
    private JobService jobService;

    @Bean
    public JobStateChangeListener logJobEventListener() {
        return new LogJobStateChangeListener();
    }

    @Bean
    public JobStateChangeListener persistenceJobEventListener() {
        return new PersistenceJobStateChangeListener(jobService);
    }

}
