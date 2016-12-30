package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.VersionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the application's version.
 *
 * The following application properties are used to configure this:
 * <ul>
 *     <li>edison.status.vcs.version: The VCS version number. Default is 'unknown'</li>
 *     <li>edison.status.vcs.commit: The VCS commit hash. Default is 'unknown'</li>
 *     <li>edison.status.vcs.url-template: An URL template to create a link to VCS server. Default is ''.
 *     The template may contain {commit} and/or {version} placeholders that are replaced by the version or commit</li>
 * </ul>
 */
@Configuration
public class VersionInfoConfiguration {

    @Value("${edison.status.vcs.version:unknown}")
    private String version;
    @Value(("${edison.status.vcs.commit:unknown}"))
    private String commit;
    @Value(("${edison.status.vcs.url-template:}"))
    private String vcsUrlTemplate;

    @Bean
    @ConditionalOnMissingBean(VersionInfo.class)
    public VersionInfo versionInfo() {
        return VersionInfo.versionInfo(version, commit, vcsUrlTemplate);
    }

}
