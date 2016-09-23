package de.otto.edison.cache;

import de.otto.edison.cache.configuration.GuavaCacheConfig;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GuavaCacheConfigTest {

    @Test
    public void shouldParseSpecification() {
        // given
        final GuavaCacheConfig cacheConfig = new GuavaCacheConfig("test", "initialCapacity=1,recordStats");
        // then
        assertThat(cacheConfig.cacheName, is("test"));
        assertThat(cacheConfig.toMap().get("initialCapacity"), is("1"));
        assertThat(cacheConfig.toMap().get("recordStats"), is("true"));
    }
}