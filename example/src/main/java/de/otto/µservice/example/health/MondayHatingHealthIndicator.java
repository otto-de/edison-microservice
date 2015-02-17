package de.otto.µservice.example.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

import static java.time.LocalDate.now;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

/**
 * An example HealthIndicator that is constantly down on mondays.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class MondayHatingHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        if (now().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            return down().build();
        }
        return up().build();
    }

}
