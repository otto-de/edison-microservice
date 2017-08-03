package de.otto.edison.status.configuration;

import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.CommonPropertyInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;

@Beta
@Configuration
@ConfigurationProperties(prefix="edison")
public class CommonPropertiesConfiguration {

    private List<CommonPropertyInfo> commonProperties = new LinkedList<>();

    public void setCommonProperties(final List<CommonPropertyInfo> commonProperties) {
        this.commonProperties = commonProperties;
    }

    public List<CommonPropertyInfo> getCommonProperties() {
        return commonProperties;
    }

    @Bean
    public List<CommonPropertyInfo> edisonCommonProperties() {
        return commonProperties;
    }

}
