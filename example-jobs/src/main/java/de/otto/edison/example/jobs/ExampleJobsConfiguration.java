package de.otto.edison.example.jobs;

import de.otto.edison.jobs.repository.KeepLastJobs;
import de.otto.edison.jobs.repository.StopDeadJobs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static java.time.Clock.systemDefaultZone;

/**
 * @author Guido Steinacker
 * @since 01.03.15
 */
@Configuration
public class ExampleJobsConfiguration {

    @Bean
    public KeepLastJobs keepLast10FooJobsCleanupStrategy() {
        return new KeepLastJobs(10, Optional.empty());
    }

    @Bean
    public StopDeadJobs stopDeadJobsStrategy() {
        return new StopDeadJobs(60, systemDefaultZone());
    }
}
