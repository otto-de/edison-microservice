package de.otto.edison.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.List;
import java.util.Properties;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.requireNonNull;
import static software.amazon.awssdk.services.ssm.model.ParameterType.SECURE_STRING;

public class ParamStorePropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(ParamStorePropertySourcePostProcessor.class);

    private static final String PARAMETER_STORE_PROPERTY_SOURCE = "paramStorePropertySource";

    private ParamStoreProperties properties;
    private final SsmClient ssmClient;

    @Autowired
    public ParamStorePropertySourcePostProcessor(final SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final Properties propertiesSource = new Properties();

        final GetParametersByPathRequest.Builder requestBuilder = GetParametersByPathRequest
                .builder()
                .path(properties.getPath())
                .recursive(true)
                .withDecryption(true);

        final GetParametersByPathResponse firstPage = ssmClient.getParametersByPath(requestBuilder.build());
        addParametersToPropertiesSource(propertiesSource, firstPage.parameters());
        String nextToken = firstPage.nextToken();

        while (!isNullOrEmpty(nextToken)) {
            final GetParametersByPathResponse nextPage = ssmClient.getParametersByPath(requestBuilder
                    .nextToken(nextToken)
                    .build()
            );
            addParametersToPropertiesSource(propertiesSource, nextPage.parameters());
            nextToken = nextPage.nextToken();
        }

        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        if (properties.isAddWithLowestPrecedence()) {
            propertySources.addLast(new PropertiesPropertySource(PARAMETER_STORE_PROPERTY_SOURCE, propertiesSource));
        } else {
            propertySources.addFirst(new PropertiesPropertySource(PARAMETER_STORE_PROPERTY_SOURCE, propertiesSource));
        }
    }

    private void addParametersToPropertiesSource(final Properties propertiesSource, final List<Parameter> parameters) {
        parameters.forEach(p -> {
            final String name = p.name().substring(properties.getPath().length() + 1);
            final String loggingValue = SECURE_STRING == p.type() ? "*****" : p.value();
            LOG.info("Loaded '" + name + "' from ParametersStore, value='" + loggingValue + "', length=" + p.value().length());

            propertiesSource.setProperty(name, p.value());
        });
    }

    @Override
    public void setEnvironment(final Environment environment) {
        final String pathProperty = "edison.env.paramstore.path";
        final String path = requireNonNull(environment.getProperty(pathProperty),
                "Property '" + pathProperty + "' must not be null");
        properties = new ParamStoreProperties();
        properties.setAddWithLowestPrecedence(
                parseBoolean(environment.getProperty("edison.env.paramstore.addWithLowestPrecedence", "false")));
        properties.setPath(path);
    }

}
