package de.otto.edison.togglz;

import de.otto.edison.testsupport.togglz.FeatureManagerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.cache.CachingStateRepository;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestServer.class})
@Profile("test")
class DefaultTogglzConfigTest {

    @Autowired
    private TogglzConfig togglzConfig;

    @BeforeEach
    void setUp() {
        FeatureManagerSupport.allEnabledFeatureConfig(TestFeatures.class);
    }

    @Test
    void shouldCreateTogglzConfigBySpring() {
        assertThat(togglzConfig, is(not(nullValue())));
        assertThat(togglzConfig.getFeatureClass(), typeCompatibleWith(TestFeatures.class));
        assertThat(togglzConfig.getStateRepository(), is(not(nullValue())));
        assertThat(togglzConfig.getStateRepository(), is(instanceOf(CachingStateRepository.class)));
        assertThat(togglzConfig.getUserProvider(), is(not(nullValue())));
    }

    @Test
    void shouldProvideToggleStateWhichIsActiveByDefaultInTests() {
        assertThat(TestFeatures.TEST_FEATURE.isActive(), is(true));
    }
}