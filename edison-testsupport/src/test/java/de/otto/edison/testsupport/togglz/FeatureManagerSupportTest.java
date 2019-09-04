package de.otto.edison.testsupport.togglz;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

class FeatureManagerSupportTest {

    @Test
    void shouldEnableAllFeaturesThatAreNotInactiveInTests() {
        //given

        //when
        FeatureManagerSupport.allEnabledFeatureConfig(TestFeatures.class);

        //then
        assertThat(getFeatureManager().isActive(TestFeatures.INACTIVE), is(false));
        assertThat(getFeatureManager().isActive(TestFeatures.ACTIVE), is(true));
    }

    @Test
    void shouldDisableAllFeatures() {
        //given
        FeatureManagerSupport.allEnabledFeatureConfig(TestFeatures.class);

        //when
        FeatureManagerSupport.allDisabledFeatureConfig(TestFeatures.class);

        //then
        assertThat(getFeatureManager().isActive(TestFeatures.INACTIVE), is(false));
        assertThat(getFeatureManager().isActive(TestFeatures.ACTIVE), is(false));
    }

    public enum TestFeatures implements Feature {
        @Label("should be inactive [inactiveInTests]")
        INACTIVE,
        ACTIVE,
    }
}