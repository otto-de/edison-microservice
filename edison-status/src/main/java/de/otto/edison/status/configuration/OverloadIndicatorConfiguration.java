package de.otto.edison.status.configuration;

import de.otto.edison.status.indicator.load.EverythingFineStrategy;
import de.otto.edison.status.indicator.load.OverloadDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OverloadIndicatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OverloadDetector defaultStrategy() {
        return new EverythingFineStrategy();
    }

}
