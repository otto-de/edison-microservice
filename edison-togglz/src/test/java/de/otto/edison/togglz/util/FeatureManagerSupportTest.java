package de.otto.edison.togglz.util;

import de.otto.edison.togglz.TestFeatures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;

import java.util.Optional;

import static de.otto.edison.testsupport.togglz.FeatureManagerSupport.allEnabledFeatureConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FeatureManagerSupportTest {

    @BeforeEach
    void setUp() {
        allEnabledFeatureConfig(TestFeatures.class);
    }

    @Test
    void shouldReturnTheCorrectFeature() {
        assertThat(FeatureManagerSupport.getFeatureFromName("TEST_FEATURE").get(), is(TestFeatures.TEST_FEATURE));
    }

    @Test
    void shouldReturnTheAnEmptyFeatureIfNameisNotKnown() {
        Optional<Feature> unknwonFeature = FeatureManagerSupport.getFeatureFromName("UNKNWON_FEATURE");
        assertFalse(unknwonFeature.isPresent());
    }
}