package de.otto.edison.status.domain;

import de.otto.edison.status.configuration.VersionInfoProperties;
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

    private VersionInfo(final VersionInfoProperties versionInfoProperties) {
        this.version = versionInfoProperties.getVersion();
        this.commit = versionInfoProperties.getCommit();
        this.url = versionInfoProperties.getUrlTemplate().replace("{commit}", commit).replace("{version}", version);
    }

    public static VersionInfo versionInfo(final VersionInfoProperties versionInfoProperties) {
        return new VersionInfo(versionInfoProperties);
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

    @Override
    public String toString() {
        return "VersionInfo{" +
                "version='" + version + '\'' +
                ", commit='" + commit + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
