package de.otto.edison.validation.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class ValidationConfiguration {

    @Bean
    public ResourceBundleMessageSource edisonValidationMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("ValidationMessages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

}
