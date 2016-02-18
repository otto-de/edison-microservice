package de.otto.edison.cache.configuration;

import org.springframework.cache.support.AbstractCacheManager;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class GuavaCacheConfigurationTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailOnErrorInSpecification() {
        // given
        final GuavaCacheConfiguration configuration = new GuavaCacheConfiguration();
        // when
        configuration.cacheConfigs = asList(new GuavaCacheConfig("test", "no_such_property=1"));
    }

    @Test
    public void shouldConfigureCacheManagerFromRegisteredGuavaCacheConfigs() {
        // given
        final GuavaCacheConfiguration configuration = new GuavaCacheConfiguration();
        configuration.cacheConfigs = asList(new GuavaCacheConfig("test", "initialCapacity=1"));

        // when
        final AbstractCacheManager cacheManager = (AbstractCacheManager) configuration.cacheManager();
        cacheManager.initializeCaches();

        // then
        assertThat(cacheManager.getCache("test"), is(notNullValue()));
    }
}