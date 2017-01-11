package de.otto.edison.metrics.load;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.annotations.Beta;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Allows switching to different load detection strategies by configuring
 * the property <code>edison.metrics.load.strategy</code> which has to hold
 * a class name implementing {@link LoadDetector}.
 *
 * Customization is also possible without any help of this auto-configuration
 * mechanism, if you expose a bean implementing {@link LoadDetector} on your own.
 *
 * @author Niko Schmuck
 * @since 03.03.2015
 */
@Beta
@Configuration
@ConditionalOnMissingBean(LoadDetector.class)
@ConditionalOnProperty(prefix = "edison.metrics.load", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MetricsLoadProperties.class)
public class MetricsLoadAutoConfiguration {

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private MetricsLoadProperties properties;

    @Bean
    public LoadDetector loadDetector() {
        LoadDetector detector;
        if (properties.getStrategy() != null) {
            detector = BeanUtils.instantiate(properties.getStrategy());
            detector.initialize(metricRegistry, properties);
        } else {
            detector = new EverythingFineStrategy();
        }
        return detector;
    }

}
