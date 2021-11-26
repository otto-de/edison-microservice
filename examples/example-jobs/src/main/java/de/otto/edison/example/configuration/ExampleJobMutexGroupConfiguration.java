package de.otto.edison.example.configuration;

import de.otto.edison.jobs.service.JobMutexGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExampleJobMutexGroupConfiguration {

    @Bean
    public JobMutexGroup mutualExclusion() {
        return new JobMutexGroup("barFizzle", "Bar", "Fizzle");
    }

}
