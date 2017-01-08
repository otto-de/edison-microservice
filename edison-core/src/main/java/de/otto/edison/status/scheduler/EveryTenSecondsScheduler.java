package de.otto.edison.status.scheduler;

import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.scheduling.annotation.Scheduled;

public final class EveryTenSecondsScheduler implements Scheduler{

    private static final int TEN_SECONDS = 10 * 1000;

    private final ApplicationStatusAggregator aggregator;

    public EveryTenSecondsScheduler(final ApplicationStatusAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @Scheduled(fixedDelay = TEN_SECONDS)
    public void update() {
        aggregator.update();
    }
}
