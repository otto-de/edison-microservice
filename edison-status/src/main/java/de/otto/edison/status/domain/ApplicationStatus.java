package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Immutable
public final class ApplicationStatus {

    private final ApplicationInfo applicationInfo;
    private final SystemInfo systemInfo;
    private final VersionInfo versionInfo;
    private final Status status;
    private final List<StatusDetail> statusDetails;

    private ApplicationStatus(final ApplicationInfo applicationInfo,
                              final SystemInfo systemInfo,
                              final VersionInfo versionInfo,
                              final List<StatusDetail> details) {
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
        this.applicationInfo = applicationInfo;
        this.systemInfo = systemInfo;
        this.versionInfo = versionInfo;
    }

    public static ApplicationStatus applicationStatus(final ApplicationInfo applicationInfo,
                                                      final SystemInfo systemInfo,
                                                      final VersionInfo versionInfo,
                                                      final List<StatusDetail> details) {
        return new ApplicationStatus(applicationInfo, systemInfo, versionInfo, details);
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public Status getStatus() {
        return status;
    }

    public List<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationStatus that = (ApplicationStatus) o;

        if (applicationInfo != null ? !applicationInfo.equals(that.applicationInfo) : that.applicationInfo != null)
            return false;
        if (systemInfo != null ? !systemInfo.equals(that.systemInfo) : that.systemInfo != null) return false;
        if (versionInfo != null ? !versionInfo.equals(that.versionInfo) : that.versionInfo != null) return false;
        if (status != that.status) return false;
        return !(statusDetails != null ? !statusDetails.equals(that.statusDetails) : that.statusDetails != null);

    }

    @Override
    public int hashCode() {
        int result = applicationInfo != null ? applicationInfo.hashCode() : 0;
        result = 31 * result + (systemInfo != null ? systemInfo.hashCode() : 0);
        result = 31 * result + (versionInfo != null ? versionInfo.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (statusDetails != null ? statusDetails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationStatus{" +
                "applicationInfo=" + applicationInfo +
                ", systemInfo=" + systemInfo +
                ", versionInfo=" + versionInfo +
                ", status=" + status +
                ", statusDetails=" + statusDetails +
                '}';
    }
}
