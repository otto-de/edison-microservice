package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;
import org.springframework.scheduling.annotation.Scheduled;

public class CachingApplicationStatusAggregator implements ApplicationStatusAggregator {
    public static final int TEN_SECONDS = 10 * 1000;

    private final ApplicationStatusAggregator aggregator;
    private volatile ApplicationStatus cachedStatus = null;

    public CachingApplicationStatusAggregator(ApplicationStatusAggregator aggregator) {
        this.aggregator = aggregator;
        update();
    }

    @Override
    public ApplicationStatus aggregate() {
        return cachedStatus;
    }

    @Scheduled(initialDelay = TEN_SECONDS, fixedDelay = TEN_SECONDS)
    public void update() {
        cachedStatus = aggregator.aggregate();
    }
}
