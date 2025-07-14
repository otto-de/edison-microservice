package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.RemoteTogglzConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.PropertiesPropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
@ConditionalOnProperty(prefix = "edison.togglz", name = "local.enabled", havingValue = "true")
public class LocalTogglzConfiguration implements RemoteTogglzConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LocalTogglzConfiguration.class);

    @Bean
    public StateRepository stateRepository(ApplicationContext applicationContext) {
        LOG.info("========================");
        LOG.info("Using PropertyBasedStateRepository");
        LOG.info("========================");

        Properties featureProperties = getPropertiesFromFeaturesPrefix(applicationContext);

        PropertiesPropertySource propertySource = new PropertiesPropertySource(featureProperties);
        return new PropertyBasedStateRepository(propertySource);
    }

    private static Properties getPropertiesFromFeaturesPrefix(ApplicationContext applicationContext) {
        Properties props = new Properties();
        Map<String, String> propsAsMap = Binder.get(applicationContext.getEnvironment())
                .bind("edison.togglz.local.features", Bindable.mapOf(String.class, String.class))
                .orElse(Collections.emptyMap());
        for (Map.Entry<String, String> entry : propsAsMap.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        return props;
    }
}
