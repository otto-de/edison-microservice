package de.otto.edison.status.domain;

/**
 * VCS information about the current version of the application.
 *
 * @author Guido Steinacker
 * @since 04.03.15
 */
public class VersionInfo {

    private final String version;
    private final String commit;
    private final String vcsUrl;

    private VersionInfo(final String version, final String commit, final String vcsUrlTemplate) {
        this.version = version;
        this.commit = commit;
        this.vcsUrl = vcsUrlTemplate.isEmpty() ? "" : vcsUrlTemplate.replace("{commit}", commit);
    }

    public static VersionInfo versionInfo(final String version, final String commit, final String vcsUrlTemplate) {
        return new VersionInfo(version, commit, vcsUrlTemplate);
    }

    public String getCommit() {
        return commit;
    }

    public String getVersion() {
        return version;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "commit='" + commit + '\'' +
                ", version='" + version + '\'' +
                ", vcsUrl='" + vcsUrl + '\'' +
                '}';
    }
}
