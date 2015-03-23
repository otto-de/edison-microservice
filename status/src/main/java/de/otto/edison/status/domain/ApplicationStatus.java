package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Immutable
public final class ApplicationStatus {

    private final String name;
    private final Status status;
    private final List<StatusDetail> statusDetails;
    private final VersionInfo versionInfo;
    private final String hostName;

    private ApplicationStatus(final String applicationName,
                              final VersionInfo versionInfo,
                              final List<StatusDetail> details,
                              final String hostName) {
        this.name = applicationName;
        this.versionInfo = versionInfo;
        this.hostName = hostName;
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
    }

    public static ApplicationStatus applicationStatus(final String applicationName,
                                                      final VersionInfo versionInfo,
                                                      final List<StatusDetail> details,
                                                      final String hostName) {
        return new ApplicationStatus(applicationName, versionInfo, details, hostName);
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public String getHostName() {
        return hostName;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public List<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

}
