package de.otto.edison.about.spec;

import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.VersionInfo;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Information about this Edison microservice, containing facts about the application, version, system, team
 * and dependencies to other services.
 *
 * Created by guido on 06.01.16.
 */
@Beta
@Immutable
public final class About {
    private static final TeamInfo UNKNOWN_TEAM = TeamInfo.teamInfo("unknown", "not yet configured", "not yet configured");

    public final ApplicationInfo application;
    public final VersionInfo vcs;
    public final SystemInfo system;
    public final TeamInfo team;
    public final List<ServiceSpec> serviceSpecs;

    private About(final ApplicationInfo applicationInfo,
                  final VersionInfo versionInfo,
                  final SystemInfo systemInfo,
                  final Optional<TeamInfo> teamInfo,
                  final Optional<List<ServiceSpec>> serviceSpecs) {

        this.application = applicationInfo;
        this.vcs = versionInfo;
        this.system = systemInfo;
        this.team = teamInfo.orElse(UNKNOWN_TEAM);
        this.serviceSpecs = serviceSpecs.orElse(emptyList())
                .stream()
                .sorted(comparing(spec->spec.appId))
                .collect(toList());
    }

    public static About about(final ApplicationInfo applicationInfo,
                              final VersionInfo versionInfo,
                              final SystemInfo systemInfo,
                              final Optional<TeamInfo> teamInfo,
                              final Optional<List<ServiceSpec>> serviceSpecs) {
        return new About(applicationInfo, versionInfo, systemInfo, teamInfo, serviceSpecs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        About about = (About) o;

        if (application != null ? !application.equals(about.application) : about.application != null) return false;
        if (vcs != null ? !vcs.equals(about.vcs) : about.vcs != null) return false;
        if (system != null ? !system.equals(about.system) : about.system != null) return false;
        if (team != null ? !team.equals(about.team) : about.team != null) return false;
        return !(serviceSpecs != null ? !serviceSpecs.equals(about.serviceSpecs) : about.serviceSpecs != null);

    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (vcs != null ? vcs.hashCode() : 0);
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (team != null ? team.hashCode() : 0);
        result = 31 * result + (serviceSpecs != null ? serviceSpecs.hashCode() : 0);
        return result;
    }
}
