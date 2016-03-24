package de.otto.edison.example.jobs;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
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
    public AsyncHttpClient httpClient() {
        return new AsyncHttpClient(new AsyncHttpClientConfig.Builder()
                .build());
    }

    @Bean
    public KeepLastJobs keepLast10FooJobsCleanupStrategy() {
        return new KeepLastJobs(10);
    }

    @Bean
    public StopDeadJobs stopDeadJobsStrategy() {
        return new StopDeadJobs(60, systemDefaultZone());
    }

}
