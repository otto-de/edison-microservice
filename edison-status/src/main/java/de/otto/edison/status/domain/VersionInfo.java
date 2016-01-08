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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionInfo that = (VersionInfo) o;

        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (commit != null ? !commit.equals(that.commit) : that.commit != null) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);

    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (commit != null ? commit.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
