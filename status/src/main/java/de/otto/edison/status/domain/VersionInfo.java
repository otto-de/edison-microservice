package de.otto.edison.status.domain;

/**
 * VCS information about the current version of the application.
 *
 * @author Guido Steinacker
 * @since 04.03.15
 */
public final class VersionInfo {

    private final String version;
    private final String commit;

    private VersionInfo(String version, String commit) {
        this.version = version;
        this.commit = commit;
    }

    public static VersionInfo versionInfo(final String version, final String commit) {
        return new VersionInfo(version, commit);
    }

    public String getCommit() {
        return commit;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "commit='" + commit + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
