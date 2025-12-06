package de.otto.edison.health.indicator;

import net.jcip.annotations.ThreadSafe;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import static org.springframework.boot.health.contributor.Health.up;

@ThreadSafe
public final class ApplicationHealthIndicator implements HealthIndicator {

    private volatile Health lastHealth = up().build();

    public void indicateHealth(final Health health) {
        this.lastHealth = health;
    }

    @Override
    public Health health() {
        return lastHealth;
    }
}
