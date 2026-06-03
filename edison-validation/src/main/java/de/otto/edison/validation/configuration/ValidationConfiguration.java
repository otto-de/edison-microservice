package de.otto.edison.validation.configuration;

import de.otto.edison.validation.web.ErrorHalRepresentationFactory;
import de.otto.edison.validation.web.ValidationExceptionHandler;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
public class ValidationConfiguration {

    @Bean
    public AbstractMessageSource edisonValidationMessageSource() {
        AggregateResourceBundleMessageSource source = new AggregateResourceBundleMessageSource();
        source.setBasename("ValidationMessages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        PlatformResourceBundleLocator resourceBundleLocator =
                new PlatformResourceBundleLocator(NonELResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES, null, true);

        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setMessageInterpolator(new NonELResourceBundleMessageInterpolator(resourceBundleLocator));
        return factoryBean;
    }

    @Bean
    public ErrorHalRepresentationFactory errorHalRepresentationFactory(
            final AbstractMessageSource edisonValidationMessageSource,
            final ObjectMapper objectMapper,
            @Value("${edison.validation.error-profile:http://spec.otto.de/profiles/error}") final String errorProfile) {
        return new ErrorHalRepresentationFactory(edisonValidationMessageSource, objectMapper, errorProfile);
    }

    @Bean
    public ValidationExceptionHandler validationExceptionHandler(final ErrorHalRepresentationFactory errorHalRepresentationFactory) {
        return new ValidationExceptionHandler(errorHalRepresentationFactory);
    }}
