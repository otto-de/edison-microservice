package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static java.time.Clock.systemDefaultZone;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;

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

    @Bean
    public JobDefinition fooJobDefinition() {
        return new JobDefinition() {
            @Override
            public URI triggerUri() {
                return URI.create("/internal/jobs/FooJob");
            }

            @Override
            public String jobType() {
                return "FooJob";
            }

            @Override
            public String jobName() {
                return "An example job named Foo";
            }

            @Override
            public Optional<Duration> fixedDelay() {
                return Optional.of(ofHours(1));
            }

            @Override
            public Optional<Duration> maxAge() {
                return Optional.of(ofHours(3));
            }

            @Override
            public int retries() {
                return 6;
            }

            @Override
            public Optional<Duration> retryDelay() {
                return Optional.of(ofMinutes(10));
            }
        };
    }
}
