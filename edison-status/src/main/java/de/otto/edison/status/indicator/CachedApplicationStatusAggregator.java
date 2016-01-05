package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static java.net.InetAddress.getLocalHost;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public class CachedApplicationStatusAggregator implements ApplicationStatusAggregator {

    private volatile ApplicationStatus cachedStatus;

    private final ApplicationInfo applicationInfo;
    private final SystemInfo systemInfo;
    private final VersionInfo versionInfo;
    private final List<StatusDetailIndicator> indicators;

    public CachedApplicationStatusAggregator(final ApplicationInfo applicationInfo,
                                             final SystemInfo systemInfo,
                                             final VersionInfo versionInfo,
                                             final List<StatusDetailIndicator> indicators) {
        this.applicationInfo = applicationInfo;
        this.systemInfo = systemInfo;
        this.versionInfo = versionInfo;
        this.indicators = unmodifiableList(new ArrayList<>(indicators));
        this.cachedStatus = applicationStatus(applicationInfo, systemInfo, versionInfo, Collections.<StatusDetail>emptyList());
    }

    @Override
    public ApplicationStatus aggregatedStatus() {
        return cachedStatus;
    }

    @Override
    public void update() {
        this.cachedStatus = calcApplicationStatus();
    }

    private ApplicationStatus calcApplicationStatus() {
        final List<StatusDetail> allDetails = indicators
                .stream()
                .flatMap(i->i.statusDetails().stream())
                .collect(toList());
        return applicationStatus(applicationInfo, systemInfo, versionInfo, allDetails);
    }

    private String hostName() {
        try {
            final String envHost = System.getenv("HOST");
            if (envHost != null) {
                return envHost;
            } else {
                return getLocalHost().getHostName();
            }
        } catch (final UnknownHostException e) {
            return "UNKOWN";
        }
    }

}
