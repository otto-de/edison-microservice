package de.otto.edison.jobs.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guido on 18.05.16.
 */
@Configuration
class TestConfiguration {

    @Bean
    JobMutexGroup fooGroup() {
        return new JobMutexGroup("Foo", "A", "B");
    }

    @Bean JobMutexGroup barGroup() {
        return new JobMutexGroup("Bar", "B", "C");
    }
}
