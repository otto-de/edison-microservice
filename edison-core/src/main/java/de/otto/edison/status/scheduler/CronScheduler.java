package de.otto.edison.status.scheduler;

import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.scheduling.annotation.Scheduled;

public class CronScheduler implements Scheduler {

    private final ApplicationStatusAggregator aggregator;

    public CronScheduler(final ApplicationStatusAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @Scheduled(cron = "${edison.status.scheduler.cron}")
    public void update() {
        aggregator.update();
    }
}
