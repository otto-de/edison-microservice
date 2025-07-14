package de.otto.edison.acceptance.togglz;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.TestFeatures;
import de.otto.edison.togglz.TestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.NoOpUserProvider;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestServer.class})
@TestPropertySource
        (properties = {
                "edison.togglz.local.enabled=true",
                "edison.togglz.local.features.TEST_FEATURE=true"
        })
@ContextConfiguration(classes = {LocalTogglzAcceptanceTest.TogglzConfiguration.class})
public class LocalTogglzAcceptanceTest {

    @Autowired
    private FeatureManager featureManager;


    @Test
    public void shouldGetTheCorrectFeatureStateFromProperties() {
        // when
        FeatureState featureStateFeature1 = featureManager.getFeatureState(TestFeatures.TEST_FEATURE);
        FeatureState featureStateFeature2 = featureManager.getFeatureState(TestFeatures.TEST_FEATURE_2);

        // then
        assertThat(featureStateFeature1.isEnabled(), is(true));
        assertThat(featureStateFeature2.isEnabled(), is(false));
    }

    @Configuration
    static class TogglzConfiguration{

        @Bean
        @Profile("test")
        public TogglzConfig togglzConfig(StateRepository stateRepository) {
            return new DefaultTogglzConfig(
                    stateRepository,
                    new NoOpUserProvider(),
                    () ->TestFeatures.class
            );
        }

    }
}
