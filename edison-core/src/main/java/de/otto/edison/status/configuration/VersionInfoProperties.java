package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  The following application properties are used to configure version info:
 * <ul>
 *     <li>edison.status.vcs.version: The VCS version number. Default is 'unknown'</li>
 *     <li>edison.status.vcs.commit: The VCS commit hash. Default is 'unknown'</li>
 *     <li>edison.status.vcs.url-template: An URL template to create a link to VCS server. Default is ''.
 *     The template may contain {commit} and/or {version} placeholders that are replaced by the version or commit</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.status.vcs")
public class VersionInfoProperties {
    private String version = "unknown";
    private String commit = "unknown";
    private String urlTemplate = "";

    /**
     * Used for testing purposes.
     *
     * @param version vcs version
     * @param commit vcs commit number
     * @param urlTemplate template used to generate links to the vcs server
     * @return VersionInfoProperties
     */
    public static VersionInfoProperties versionInfoProperties(final String version, final String commit, final String urlTemplate) {
        final VersionInfoProperties p = new VersionInfoProperties();
        p.version = version;
        p.commit = commit;
        p.urlTemplate = urlTemplate;
        return p;
    }

    public String getVersion() {
        return version;
    }

    public String getCommit() {
        return commit;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }
}
