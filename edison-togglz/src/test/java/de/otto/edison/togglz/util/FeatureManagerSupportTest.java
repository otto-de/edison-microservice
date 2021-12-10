package de.otto.edison.togglz.util;

import de.otto.edison.togglz.TestFeatures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;

import java.util.Optional;

import static de.otto.edison.testsupport.togglz.FeatureManagerSupport.allEnabledFeatureConfig;
import static org.assertj.core.api.Assertions.assertThat;

class FeatureManagerSupportTest {

    @BeforeEach
    void setUp() {
        allEnabledFeatureConfig(TestFeatures.class);
    }

    @Test
    void shouldReturnTheCorrectFeature() {
        assertThat(FeatureManagerSupport.getFeatureFromName("TEST_FEATURE").get()).isEqualTo(TestFeatures.TEST_FEATURE);
    }

    @Test
    void shouldReturnTheAnEmptyFeatureIfNameisNotKnown() {
        Optional<Feature> unknownFeature = FeatureManagerSupport.getFeatureFromName("UNKNWON_FEATURE");
        assertThat(unknownFeature.isPresent()).isFalse();
    }
}
