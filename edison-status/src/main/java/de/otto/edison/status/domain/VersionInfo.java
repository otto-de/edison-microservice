package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

/**
 * VCS information about the current version of the application.
 *
 * @author Guido Steinacker
 * @since 04.03.15
 */
@Immutable
public class VersionInfo {

    public final String version;
    public final String commit;
    public final String url;

    private VersionInfo(final String version, final String commit, final String vcsUrlTemplate) {
        this.version = version;
        this.commit = commit;
        this.url = vcsUrlTemplate.isEmpty() ? "" : vcsUrlTemplate.replace("{commit}", commit).replace("{version}", version);
    }

    public static VersionInfo versionInfo(final String version, final String commit, final String vcsUrlTemplate) {
        return new VersionInfo(version, commit, vcsUrlTemplate);
    }
}
