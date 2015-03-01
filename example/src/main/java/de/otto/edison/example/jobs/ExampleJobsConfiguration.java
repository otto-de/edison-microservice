package de.otto.edison.example.jobs;

import de.otto.edison.jobs.repository.JobCleanupStrategy;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.KeepLastJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static java.util.Optional.of;

/**
 * @author Guido Steinacker
 * @since 01.03.15
 */
@Configuration
public class ExampleJobsConfiguration {

    @Bean
    public JobCleanupStrategy keepLast10FooJobsCleanupStrategy() {
        return new KeepLastJobs(10, of(ExampleJobs.FOO));
    }

}
