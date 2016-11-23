package de.otto.edison.cache.configuration;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class CacheRegistryTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnErrorInSpecification() {
        new DefaultCacheRegistry(asList(new CaffeineCacheConfig("test", "no_such_property=1")));
    }

    @Test
    public void shouldConfigureCacheManagerFromRegisteredCaffeineCacheConfigs() {
        DefaultCacheRegistry cacheRegistry = new DefaultCacheRegistry(asList(new CaffeineCacheConfig("test", "initialCapacity=1")));

        assertThat(cacheRegistry.getCache("test"), is(notNullValue()));
    }
}
