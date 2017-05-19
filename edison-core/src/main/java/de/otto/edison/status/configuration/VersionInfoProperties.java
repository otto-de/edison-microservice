package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  The following application properties are used to configure version info:
 * <ul>
 *     <li>edison.status.vcs.version: The VCS version number. Default is 'unknown'</li>
 *     <li>edison.status.vcs.commit: The VCS commit hash. Default is 'unknown'</li>
 *     <li>edison.status.vcs.url-template: An URL template to create a link to VCS server. Default is ''.
 *     The template may contain {commit} and/or {version} placeholders that are replaced by the version or commit</li>
 *     <li>edison.status.vcs.commit-time: The time of the commit. Default is ''.
 *     <li>edison.status.vcs.user-name: The user name of the commit. Default is ''.
 *     <li>edison.status.vcs.user-email: The user email of the commit. Default is ''.
 *     <li>edison.status.vcs.message-short: The short message of the commit. Default is ''.
 *     <li>edison.status.vcs.message-full: The full message of the commit. Default is ''.
 *     <li>edison.status.vcs.branch: The branch of the commit. Default is ''.
 * </ul>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.status.vcs")
public class  VersionInfoProperties {
    private String version = "unknown";
    private String commit = "unknown";
    private String urlTemplate = "";
    private String commitTime = "";
    private String userName = "";
    private String userEmail = "";
    private String messageShort = "";
    private String messageFull = "";
    private String branch = "";

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

    public String getCommitId() {
        return commit;
    }

    /**
     * Return the abbreviated id of the commit or {@code null}.
     *
     * @return the short commit id
     */
    public String getCommitIdAbbrev() {
        final String id = getCommitId();
        return (id.length() > 7 ? id.substring(0, 7) : id);
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public String getCommit() {
        return commit;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessageShort() {
        return messageShort;
    }

    public void setMessageShort(String messageShort) {
        this.messageShort = messageShort;
    }

    public String getMessageFull() {
        return messageFull;
    }

    public void setMessageFull(String messageFull) {
        this.messageFull = messageFull;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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
