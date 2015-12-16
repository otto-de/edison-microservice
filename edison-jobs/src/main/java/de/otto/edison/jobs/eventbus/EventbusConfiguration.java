package de.otto.edison.jobs.eventbus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventbusConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventPublisher() {
        return new EventPublisher(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(LogJobEventListener.class)
    public LogJobEventListener logEventListener() {
        return new LogJobEventListener();
    }
}
