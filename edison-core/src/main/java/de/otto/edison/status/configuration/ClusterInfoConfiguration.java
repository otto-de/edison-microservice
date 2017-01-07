package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ClusterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

@Configuration
@EnableConfigurationProperties(ClusterInfoProperties.class)
public class ClusterInfoConfiguration {

    private final ClusterInfoProperties clusterInfoProperties;

    @Autowired
    public ClusterInfoConfiguration(final ClusterInfoProperties clusterInfoProperties) {
        this.clusterInfoProperties = clusterInfoProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ClusterInfo.class)
    public ClusterInfo clusterInfo() {
        return new ClusterInfo(
                () -> httpHeaderValue(clusterInfoProperties.getColorHeader()),
                () -> httpHeaderValue(clusterInfoProperties.getColorStateHeader())
        );
    }

    private static String httpHeaderValue(final String header) {
        final String value;
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            value = requestAttributes.getRequest().getHeader(header);
        } else {
            value = "";
        }
        return value != null ? value : "";
    }

}
