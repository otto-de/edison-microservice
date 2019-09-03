package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.KFeatureManagerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.spi.FeatureProvider;

import java.util.Optional;

@Configuration
public class TestTogglesConfiguration {

    @Bean
    @Primary
    @Profile("test")
    public FeatureManager testFeatureManager(final Optional<FeatureProvider> featureProvider) throws Exception {

        FeatureManagerBuilder featureManagerBuilder = FeatureManagerBuilder.begin();

        featureProvider.ifPresent(featureManagerBuilder::featureProvider);

        FeatureManager featureManager = featureManagerBuilder.build();
        KFeatureManagerProvider.Companion.setInstance(featureManager);
        return featureManager;
    }
}
