package de.otto.edison.example.jobs;

import de.otto.edison.jobs.repository.JobCleanupStrategy;
import de.otto.edison.jobs.repository.KeepLastJobs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Optional.of;

/**
 * @author Guido Steinacker
 * @since 01.03.15
 */
@Configuration
public class ExampleJobsConfiguration {

    @Bean
    public JobCleanupStrategy keepLast10FooJobsCleanupStrategy() {
        return new KeepLastJobs(10);
    }

}
