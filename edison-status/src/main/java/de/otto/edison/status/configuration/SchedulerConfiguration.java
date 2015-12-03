package de.otto.edison.status.configuration;

import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.scheduler.CronScheduler;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfiguration {

    @Autowired
    private ApplicationStatusAggregator aggregator;

    @Bean
    @ConditionalOnProperty(name = "edison.status.scheduler.cron")
    public Scheduler cronScheduler() {
        return new CronScheduler(
                aggregator
        );
    }

    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler fixedDelayScheduler() {
        return new EveryTenSecondsScheduler(
                aggregator
        );
    }

}
