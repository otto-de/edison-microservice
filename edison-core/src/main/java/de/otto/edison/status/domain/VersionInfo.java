package de.otto.edison.status.domain;

import de.otto.edison.status.configuration.VersionInfoProperties;
import net.jcip.annotations.Immutable;
import org.springframework.boot.info.GitProperties;

import java.util.Objects;

/**
 * VCS information about the current version of the application.
 * <p>
 *     These information are displayed on /internal/status in HTML and JSON format.
 * </p>
 *
 * @author Guido Steinacker
 * @since 04.03.15
 */
@Immutable
public class VersionInfo {

    private static final String COMMIT_TIME = "commit.time";
    private static final String USER_NAME = "commit.user.name";
    private static final String USER_EMAIL = "commit.user.email";
    private static final String MESSAGE_SHORT = "commit.message.short";
    private static final String MESSAGE_FULL = "commit.message.full";

    /**
     * The version number of the service, or {@link #commitId}, if no version property is set.
     */
    public final String version;
    /**
     * The full VCS commit id.
     */
    public final String commitId;
    /**
     * The abbreviated commit id.
     */
    public final String commitIdAbbrev;
    /**
     * The time of the last commit.
     */
    public final String commitTime;
    /**
     * The user-name of the committer.
     */
    public final String userName;
    /**
     * The user-email of the committer.
     */
    public final String userEmail;
    /**
     * A short commit message.
     */
    public final String messageShort;
    /**
     * Full commit message.
     */
    public final String messageFull;
    /**
     * The VCS branch.
     */
    public final String branch;
    /**
     * URL pointing to the commit or version in the VCS.
     */
    public final String url;

    private VersionInfo(final VersionInfoProperties versionInfoProperties, final GitProperties gitProperties) {
        if (gitProperties != null) {
            this.commitId = gitProperties.getCommitId();
            this.commitIdAbbrev = gitProperties.getShortCommitId();
            this.branch = gitProperties.getBranch();
            this.commitTime = gitProperties.get(COMMIT_TIME);
            this.userName = gitProperties.get(USER_NAME);
            this.userEmail = gitProperties.get(USER_EMAIL);
            this.messageShort = gitProperties.get(MESSAGE_SHORT);
            this.messageFull = gitProperties.get(MESSAGE_FULL);

        } else {
            this.commitId = versionInfoProperties.getCommitId();
            this.commitIdAbbrev= versionInfoProperties.getCommitIdAbbrev();
            this.commitTime = versionInfoProperties.getCommitTime();
            this.userName = versionInfoProperties.getUserName();
            this.userEmail = versionInfoProperties.getUserEmail();
            this.messageShort = versionInfoProperties.getMessageShort();
            this.messageFull = versionInfoProperties.getMessageFull();
            this.branch = versionInfoProperties.getBranch();
        }
        this.version = Objects.toString(versionInfoProperties.getVersion(), this.commitId);
        this.url = versionInfoProperties.getUrlTemplate().replace("{commit}", commitId).replace("{version}", version);
    }

    /**
     * Creates VersionInfo from {@link VersionInfoProperties}.
     *
     * @param versionInfoProperties properties used to configure the version information.
     * @return VersionInfo
     */
    public static VersionInfo versionInfo(final VersionInfoProperties versionInfoProperties) {
        return versionInfo(versionInfoProperties, null);
    }

    /**
     * Creates VersionInfo from Spring Boot {@link GitProperties}. Missing Information ({@link #version} and
     * {@link #url})is filled from VersionInfoProperties.
     *
     * @param versionInfoProperties Edison VersionInfoProperties used for version and url
     * @param gitProperties Spring Boot GitProperties for all the other properties.
     * @return VersionInfo
     */
    public static VersionInfo versionInfo(final VersionInfoProperties versionInfoProperties,
                                          final GitProperties gitProperties) {
        return new VersionInfo(versionInfoProperties, gitProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(commitId, that.commitId) &&
                Objects.equals(commitIdAbbrev, that.commitIdAbbrev) &&
                Objects.equals(commitTime, that.commitTime) &&
                Objects.equals(url, that.url) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(messageShort, that.messageShort) &&
                Objects.equals(messageFull, that.messageFull) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, commitId, commitIdAbbrev, commitTime, url, userName, userEmail, messageShort, messageFull, branch);
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "version='" + version + '\'' +
                ", commitId='" + commitId + '\'' +
                ", commitIdAbbrev='" + commitIdAbbrev + '\'' +
                ", commitTime='" + commitTime + '\'' +
                ", url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", messageShort='" + messageShort + '\'' +
                ", messageFull='" + messageFull + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}
