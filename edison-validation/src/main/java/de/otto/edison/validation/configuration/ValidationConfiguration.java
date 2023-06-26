package de.otto.edison.validation.configuration;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
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

}
