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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationStatus that = (ApplicationStatus) o;

        if (application != null ? !application.equals(that.application) : that.application != null) return false;
        if (system != null ? !system.equals(that.system) : that.system != null) return false;
        if (vcs != null ? !vcs.equals(that.vcs) : that.vcs != null) return false;
        if (status != that.status) return false;
        return !(statusDetails != null ? !statusDetails.equals(that.statusDetails) : that.statusDetails != null);

    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (vcs != null ? vcs.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (statusDetails != null ? statusDetails.hashCode() : 0);
        return result;
    }
}
