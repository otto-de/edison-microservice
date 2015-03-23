package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.VersionInfo;

import java.util.ArrayList;
import java.util.List;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public class DefaultApplicationStatusAggregator implements ApplicationStatusAggregator {

    private final String applicationName;
    private final VersionInfo versionInfo;
    private String hostName;
    private final List<StatusDetailIndicator> indicators;

    public DefaultApplicationStatusAggregator(final String applicationName,
                                              final VersionInfo versionInfo,
                                              final List<StatusDetailIndicator> indicators,
                                              final String hostName) {
        this.applicationName = applicationName;
        this.versionInfo = versionInfo;
        this.hostName = hostName;
        this.indicators = unmodifiableList(new ArrayList<>(indicators));
    }

    @Override
    public ApplicationStatus aggregate() {
        return applicationStatus(applicationName, versionInfo, indicators
                .stream()
                .map(StatusDetailIndicator::statusDetail)
                .collect(toList()),
                hostName);
    }
}
