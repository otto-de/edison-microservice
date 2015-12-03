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
                              final String hostName,
                              final VersionInfo versionInfo,
                              final List<StatusDetail> details) {
        this.name = applicationName;
        this.versionInfo = versionInfo;
        this.hostName = hostName;
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
    }

    public static ApplicationStatus applicationStatus(final String applicationName,
                                                      final String hostName,
                                                      final VersionInfo versionInfo,
                                                      final List<StatusDetail> details) {
        return new ApplicationStatus(applicationName, hostName, versionInfo, details);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationStatus that = (ApplicationStatus) o;

        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (status != that.status) return false;
        if (statusDetails != null ? !statusDetails.equals(that.statusDetails) : that.statusDetails != null)
            return false;
        if (versionInfo != null ? !versionInfo.equals(that.versionInfo) : that.versionInfo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (statusDetails != null ? statusDetails.hashCode() : 0);
        result = 31 * result + (versionInfo != null ? versionInfo.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationStatus{" +
                "hostName='" + hostName + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", statusDetails=" + statusDetails +
                ", versionInfo=" + versionInfo +
                '}';
    }
}
