package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.JobService;
import de.otto.edison.status.configuration.SystemInfoConfiguration;
import de.otto.edison.status.domain.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Clock.systemDefaultZone;

@Configuration
public class EventBusConfiguration {


    @Autowired
    private JobService jobService;

    @Bean
    public JobEventListener logJobEventListener() {
        return new LogJobEventListener();
    }

    @Bean
    public JobEventListener persistenceJobEventListener() {
        return new PersistenceJobEventListener(jobService);
    }
}
