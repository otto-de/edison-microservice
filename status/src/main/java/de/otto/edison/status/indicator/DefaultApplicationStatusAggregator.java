package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.VersionInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private final List<StatusDetailIndicator> indicators;
    private final String hostName;

    public DefaultApplicationStatusAggregator(final String applicationName,
                                              final VersionInfo versionInfo,
                                              final List<StatusDetailIndicator> indicators) {
        this.applicationName = applicationName;
        this.versionInfo = versionInfo;
        this.indicators = unmodifiableList(new ArrayList<>(indicators));
        final String envHost = System.getenv("HOST");
        hostName = envHost != null ? envHost : localHost();
    }

    @Override
    public ApplicationStatus aggregate() {
        return applicationStatus(applicationName, hostName, versionInfo, indicators
                .stream()
                .map(StatusDetailIndicator::statusDetail)
                .collect(toList())
        );
    }

    private String localHost() {
        try {
            InetAddress localhost = java.net.InetAddress.getLocalHost();
            return localhost.getHostName();
        } catch (final UnknownHostException e) {
            return "UNKOWN";
        }
    }

}
