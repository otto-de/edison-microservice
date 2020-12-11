package de.otto.edison.validation.configuration;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfiguration {

    @Bean
    public ResourceBundleMessageSource edisonValidationMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
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
