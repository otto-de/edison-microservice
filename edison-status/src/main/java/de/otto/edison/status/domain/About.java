package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

/**
 * Information about this Edison microservice, containing facts about the application, version, system, team
 * and dependencies to other services.
 *
 * Created by guido on 06.01.16.
 */
@Beta
public class About {
    private static final TeamInfo UNKNOWN_TEAM = new TeamInfo("unknown", "not yet configured", "not yet configured");

    public final ApplicationInfo application;
    public final VersionInfo vcs;
    public final SystemInfo system;
    public final TeamInfo team;
    public final List<ServiceDependency> serviceDependencies;

    private About(final ApplicationInfo applicationInfo,
                  final VersionInfo versionInfo,
                  final SystemInfo systemInfo,
                  final Optional<TeamInfo> teamInfo,
                  final Optional<List<ServiceDependency>> serviceDependencies) {

        this.application = applicationInfo;
        this.vcs = versionInfo;
        this.system = systemInfo;
        this.team = teamInfo.orElse(UNKNOWN_TEAM);
        this.serviceDependencies = serviceDependencies.orElse(emptyList());
    }

    public static About about(final ApplicationInfo applicationInfo,
                              final VersionInfo versionInfo,
                              final SystemInfo systemInfo,
                              final Optional<TeamInfo> teamInfo,
                              final Optional<List<ServiceDependency>> serviceDependencies) {
        return new About(applicationInfo, versionInfo, systemInfo, teamInfo, serviceDependencies);
    }
}
