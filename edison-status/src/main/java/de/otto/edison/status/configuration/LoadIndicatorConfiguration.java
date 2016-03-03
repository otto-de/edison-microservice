package de.otto.edison.status.configuration;

import de.otto.edison.status.indicator.load.EverythingFineStrategy;
import de.otto.edison.status.indicator.load.LoadDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadIndicatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoadDetector defaultStrategy() {
        return new EverythingFineStrategy();
    }

}
