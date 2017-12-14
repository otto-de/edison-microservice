package de.otto.edison.logging.ui;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A BeanFactoryPostProcessor that is disabling the WebMVC part of Spring Boot Actuator endpoints, so the
 * endpoint can be replaced by some controller or other endpoint.
 *
 * @since 1.1.0
 */
class DisableEndpointPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger LOG = getLogger(DisableEndpointPostProcessor.class);
    private final String endpoint;

    DisableEndpointPostProcessor(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Disabling '{}' Endpoint", endpoint);
        disableEndpoint(beanFactory);
    }

    private void disableEndpoint(final ConfigurableListableBeanFactory beanFactory) {
        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addFirst(
                new MapPropertySource(endpoint + "PropertySource", singletonMap("endpoints." + endpoint + ".enabled", false))
        );
    }

}
