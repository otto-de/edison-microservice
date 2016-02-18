package de.otto.edison.cache.controller;

import de.otto.edison.cache.configuration.GuavaCacheConfig;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

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
        controller.cacheConfigs = asList(new GuavaCacheConfig("test", "recordStats"));

        // when:
        final Map<String, CacheInfo> json = controller.getCacheMetricsJson();

        // then:
        assertThat(json.get("test").getSpecification().get("recordStats"), is("true"));
    }
}