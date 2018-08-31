package de.otto.edison.togglz;

import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.cache.CachingStateRepository;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.hamcrest.core.Is.is;

public class DefaultTogglzConfigTest extends SpringTestBase {

    private TogglzConfig togglzConfig;

    @Before
    public void setUp() throws Exception {
        togglzConfig = applicationContext().getBean(TogglzConfig.class);

    }

    @Test
    public void shouldCreateTogglzConfigBySpring() {
        assertThat(togglzConfig, is(not(nullValue())));
        assertThat(togglzConfig.getFeatureClass(), typeCompatibleWith(TestFeatures.class));
        assertThat(togglzConfig.getStateRepository(),is(not(nullValue())));
        assertThat(togglzConfig.getStateRepository(),is(instanceOf(CachingStateRepository.class)));
        assertThat(togglzConfig.getUserProvider(),is(not(nullValue())));
    }

    @Test
    public void shouldProvideToggleStateWhichIsActiveByDefaultInTests() {
        assertThat(TestFeatures.TEST_FEATURE.isActive(),is(true));
    }
}