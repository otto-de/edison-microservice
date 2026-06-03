package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.service.JobService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class EventBusConfiguration {

    @Bean
    public JobStateChangeListener logJobEventListener() {
        return new LogJobStateChangeListener();
    }

    @Bean
    public JobStateChangeListener persistenceJobEventListener(final JobService jobService) {
        return new PersistenceJobStateChangeListener(jobService);
    }

}
