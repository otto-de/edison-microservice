package de.otto.edison.jobs.eventbus;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventbusConfiguration {

    @Bean
    @ConditionalOnMissingBean(LogJobEventListener.class)
    public LogJobEventListener logEventListener() {
        return new LogJobEventListener();
    }
}
