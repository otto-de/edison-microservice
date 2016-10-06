package de.otto.edison.cache.configuration;

import org.springframework.cache.support.AbstractCacheManager;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class CacheConfigurationTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnErrorInSpecification() {
        // given
        final CacheConfiguration configuration = new CacheConfiguration();
        // when
        configuration.cacheConfigs = asList(new CaffeineCacheConfig("test", "no_such_property=1"));
    }

    @Test
    public void shouldConfigureCacheManagerFromRegisteredCaffeineCacheConfigs() {
        // given
        final CacheConfiguration configuration = new CacheConfiguration();
        configuration.cacheConfigs = asList(new CaffeineCacheConfig("test", "initialCapacity=1"));

        // when
        final AbstractCacheManager cacheManager = (AbstractCacheManager) configuration.cacheManager();
        cacheManager.initializeCaches();

        // then
        assertThat(cacheManager.getCache("test"), is(notNullValue()));
    }
}