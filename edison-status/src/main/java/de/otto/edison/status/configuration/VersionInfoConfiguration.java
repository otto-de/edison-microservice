package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.VersionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VersionInfoConfiguration {

    @Value("${info.build.version:unknown}")
    private String version;
    @Value(("${info.build.commit:unknown}"))
    private String commit;
    @Value(("${info.build.url-template:}"))
    private String vcsUrlTemplate;

    @Bean
    @ConditionalOnMissingBean(VersionInfo.class)
    public VersionInfo versionInfo() {
        return VersionInfo.versionInfo(version, commit, vcsUrlTemplate);
    }

}
