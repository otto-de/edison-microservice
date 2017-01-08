package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Immutable
public class ApplicationStatus {

    public final ApplicationInfo application;
    public final SystemInfo system;
    public final VersionInfo vcs;
    public final TeamInfo team;
    public final ClusterInfo cluster;
    public final Status status;
    public final List<StatusDetail> statusDetails;
    public final List<ServiceSpec> serviceSpecs;

    private ApplicationStatus(final ApplicationInfo application,
                              final ClusterInfo cluster,
                              final SystemInfo system,
                              final VersionInfo vcs,
                              final TeamInfo team,
                              final List<StatusDetail> details,
                              final List<ServiceSpec> serviceSpecs) {
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
        this.application = application;
        this.cluster = cluster;
        this.system = system;
        this.vcs = vcs;
        this.team = team;
        this.serviceSpecs = serviceSpecs != null ? serviceSpecs.stream().sorted(comparing(spec->spec.name)).collect(toList()) : emptyList();
    }

    public static ApplicationStatus applicationStatus(final ApplicationInfo applicationInfo,
                                                      final ClusterInfo clusterInfo,
                                                      final SystemInfo systemInfo,
                                                      final VersionInfo versionInfo,
                                                      final TeamInfo teamInfo,
                                                      final List<StatusDetail> details,
                                                      final List<ServiceSpec> serviceSpecs) {
        return new ApplicationStatus(applicationInfo, clusterInfo, systemInfo, versionInfo, teamInfo, details, serviceSpecs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationStatus that = (ApplicationStatus) o;

        if (application != null ? !application.equals(that.application) : that.application != null) return false;
        if (system != null ? !system.equals(that.system) : that.system != null) return false;
        if (vcs != null ? !vcs.equals(that.vcs) : that.vcs != null) return false;
        if (team != null ? !team.equals(that.team) : that.team != null) return false;
        if (cluster != null ? !cluster.equals(that.cluster) : that.cluster != null) return false;
        if (status != that.status) return false;
        if (statusDetails != null ? !statusDetails.equals(that.statusDetails) : that.statusDetails != null)
            return false;
        return serviceSpecs != null ? serviceSpecs.equals(that.serviceSpecs) : that.serviceSpecs == null;
    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (vcs != null ? vcs.hashCode() : 0);
        result = 31 * result + (team != null ? team.hashCode() : 0);
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (statusDetails != null ? statusDetails.hashCode() : 0);
        result = 31 * result + (serviceSpecs != null ? serviceSpecs.hashCode() : 0);
        return result;
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
                ", serviceSpecs=" + serviceSpecs +
                '}';
    }
}
