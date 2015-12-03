package de.otto.edison.health.indicator;

import net.jcip.annotations.ThreadSafe;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import static org.springframework.boot.actuate.health.Health.up;

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
