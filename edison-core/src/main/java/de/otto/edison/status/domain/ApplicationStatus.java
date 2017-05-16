package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

@Immutable
public class ApplicationStatus {

    public final ApplicationInfo application;
    public final SystemInfo system;
    public final VersionInfo vcs;
    public final TeamInfo team;
    public final ClusterInfo cluster;
    public final Status status;
    public final List<StatusDetail> statusDetails;

    private ApplicationStatus(final ApplicationInfo application,
                              final ClusterInfo cluster,
                              final SystemInfo system,
                              final VersionInfo vcs,
                              final TeamInfo team,
                              final List<StatusDetail> details) {
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
        this.application = application;
        this.cluster = cluster;
        this.system = system;
        this.vcs = vcs;
        this.team = team;
    }

    public static ApplicationStatus applicationStatus(final ApplicationInfo applicationInfo,
                                                      final ClusterInfo clusterInfo,
                                                      final SystemInfo systemInfo,
                                                      final VersionInfo versionInfo,
                                                      final TeamInfo teamInfo,
                                                      final List<StatusDetail> details) {
        return new ApplicationStatus(applicationInfo, clusterInfo, systemInfo, versionInfo, teamInfo, details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationStatus that = (ApplicationStatus) o;
        return Objects.equals(application, that.application) &&
                Objects.equals(system, that.system) &&
                Objects.equals(vcs, that.vcs) &&
                Objects.equals(team, that.team) &&
                Objects.equals(cluster, that.cluster) &&
                status == that.status &&
                Objects.equals(statusDetails, that.statusDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, system, vcs, team, cluster, status, statusDetails);
    }

    @Override
    public String toString() {
        return "ApplicationStatus{" +
                "application=" + application +
                ", system=" + system +
                ", vcs=" + vcs +
                ", team=" + team +
                ", cluster=" + cluster +
                ", status=" + status +
                ", statusDetails=" + statusDetails +
                '}';
    }
}
