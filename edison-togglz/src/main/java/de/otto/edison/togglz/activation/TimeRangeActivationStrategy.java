package de.otto.edison.togglz.activation;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeRangeActivationStrategy implements ActivationStrategy {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");
    @Override
    public String getId() {
        return "time-range";
    }

    @Override
    public String getName() {
        return "Time Range Activation";
    }

    @Override
    public boolean isActive(final FeatureState featureState, final FeatureUser user) {
        if (featureState.isEnabled()) {
            final String fromString = featureState.getParameter("from");
            final String toString = featureState.getParameter("to");
            try {
                final ZonedDateTime fromDateTime = LocalDateTime.parse(fromString, format).atZone(ZONE_ID);
                final ZonedDateTime toDateTime = LocalDateTime.parse(toString, format).atZone(ZONE_ID);
                final ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
                return (fromDateTime.isBefore(now) && toDateTime.isAfter(now));
            } catch (RuntimeException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create("from").label("From").description("Start date and time (UTC - yyyy-MM-dd HH:mm:ss)."),
                ParameterBuilder.create("to").label("To").description("End date and time (UTC - yyyy-MM-dd HH:mm:ss).").optional(),
        };
    }
}
