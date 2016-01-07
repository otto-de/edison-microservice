package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Immutable
public final class ApplicationStatus {

    public final ApplicationInfo application;
    public final SystemInfo system;
    public final VersionInfo vcs;
    public final Status status;
    public final List<StatusDetail> statusDetails;

    private ApplicationStatus(final ApplicationInfo application,
                              final SystemInfo system,
                              final VersionInfo vcs,
                              final List<StatusDetail> details) {
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
        this.application = application;
        this.system = system;
        this.vcs = vcs;
    }

    public static ApplicationStatus applicationStatus(final ApplicationInfo applicationInfo,
                                                      final SystemInfo systemInfo,
                                                      final VersionInfo versionInfo,
                                                      final List<StatusDetail> details) {
        return new ApplicationStatus(applicationInfo, systemInfo, versionInfo, details);
    }

}
