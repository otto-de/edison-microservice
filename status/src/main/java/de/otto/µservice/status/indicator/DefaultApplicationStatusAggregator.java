package de.otto.µservice.status.indicator;

import de.otto.µservice.status.domain.ApplicationStatus;

import java.util.ArrayList;
import java.util.List;

import static de.otto.µservice.status.domain.ApplicationStatus.detailedStatus;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public class DefaultApplicationStatusAggregator implements ApplicationStatusAggregator {

    private final String applicationName;
    private final List<StatusDetailIndicator> indicators;

    public DefaultApplicationStatusAggregator(final String applicationName, final List<StatusDetailIndicator> indicators) {
        this.applicationName = applicationName;
        this.indicators = unmodifiableList(new ArrayList<>(indicators));
    }

    @Override
    public ApplicationStatus aggregate() {
        return detailedStatus(applicationName, indicators
                .stream()
                .map(StatusDetailIndicator::statusDetail)
                .collect(toList()));
    }
}
