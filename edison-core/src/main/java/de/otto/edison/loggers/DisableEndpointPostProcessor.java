package de.otto.edison.loggers;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A BeanFactoryPostProcessor that is removing Spring Boot Actuator endpoints from the application context, so the
 * endpoint can be replaced by some controller or other endpoint.
 *
 * @since 1.1.0
 */
class DisableEndpointPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static final Logger LOG = getLogger(DisableEndpointPostProcessor.class);
    private final String endpoint;
    private ConfigurableApplicationContext applicationContext;

    DisableEndpointPostProcessor(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Disabling '{}' Endpoint", endpoint);
        disableEndpoint(beanFactory);
        removeBeanDefinition();
    }

    private void disableEndpoint(final ConfigurableListableBeanFactory beanFactory) {
        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addFirst(
                new MapPropertySource(endpoint + "PropertySource", singletonMap("endpoints." + endpoint + ".enabled", false))
        );
    }

    private void removeBeanDefinition() {
        final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
        final String beanName = endpoint + "MvcEndpoint";
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
