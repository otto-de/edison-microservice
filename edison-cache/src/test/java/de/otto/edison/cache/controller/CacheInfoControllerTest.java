package de.otto.edison.cache.controller;

import com.github.benmanes.caffeine.cache.Caffeine;
import de.otto.edison.cache.configuration.CaffeineCacheConfig;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collections;
import java.util.Map;

import static com.github.benmanes.caffeine.cache.Caffeine.from;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheInfoControllerTest {

    @Test
    public void shouldHaveCacheName() {
        // given:
        final CachePublicMetrics metrics = mock(CachePublicMetrics.class);
        when(metrics.metrics()).thenReturn(asList(new Metric<Number>("cache.test.foo", 42L)));
        // and:
        final CacheInfoController controller = new CacheInfoController();
        controller.cacheMetrics = metrics;
        controller.cacheConfigs = Collections.emptyList();
        controller.caffeineCaches = Collections.emptyList();

        // when:
        final Map<String, CacheInfo> json = controller.getCacheMetricsJson();

        // then:
        assertThat(json.get("test").getName(), is("test"));
    }

    @Test
    public void shouldExposeMetrics() {
        // given:
        final CachePublicMetrics metrics = mock(CachePublicMetrics.class);
        when(metrics.metrics()).thenReturn(asList(new Metric<Number>("cache.test.foo", 42L)));
        // and:
        final CacheInfoController controller = new CacheInfoController();
        controller.cacheMetrics = metrics;
        controller.cacheConfigs = Collections.emptyList();
        controller.caffeineCaches = Collections.emptyList();

        // when:
        final Map<String, CacheInfo> json = controller.getCacheMetricsJson();

        // then:
        assertThat(json.get("test").getMetrics().get("foo"), is(42L));
    }

    @Test
    public void shouldEnrichWithSpecification() {
        // given:
        final CachePublicMetrics metrics = mock(CachePublicMetrics.class);
        when(metrics.metrics()).thenReturn(asList(new Metric<Number>("cache.test.foo", 42L)));
        // and:
        final CacheInfoController controller = new CacheInfoController();
        controller.cacheMetrics = metrics;
        controller.cacheConfigs = asList(new CaffeineCacheConfig("test", "recordStats"));
        controller.caffeineCaches = Collections.emptyList();


        // when:
        final Map<String, CacheInfo> json = controller.getCacheMetricsJson();

        // then:
        assertThat(json.get("test").getSpecification().get("recordStats"), is("true"));
    }

    @Test
    public void shouldEnrichWithSpecificationFromCaffeineCache() {
        // given:
        final CachePublicMetrics metrics = mock(CachePublicMetrics.class);
        when(metrics.metrics()).thenReturn(asList(new Metric<Number>("cache.test.foo", 42L)));
        // and:
        final CacheInfoController controller = new CacheInfoController();
        controller.cacheMetrics = metrics;
        controller.cacheConfigs = Collections.emptyList();
        controller.caffeineCaches = asList(new CaffeineCache("test", from("recordStats").build()));


        // when:
        final Map<String, CacheInfo> json = controller.getCacheMetricsJson();

        // then:
        assertThat(json.get("test").getSpecification().get("recordStats"), is("true"));
    }
}
