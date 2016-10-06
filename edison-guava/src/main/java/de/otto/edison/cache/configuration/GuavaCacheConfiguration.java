package de.otto.edison.cache.configuration;

import com.google.common.cache.CacheStats;
import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.GuavaCacheStatisticsProvider;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.cache.CacheBuilder.from;
import static java.util.stream.Collectors.toList;

/**
 * Configuration that is exposing a CacheManager and cache statistics for Guava caches.
 *
 * The CacheManager is configured with caches for all {@link GuavaCacheConfig} beans found in
 * the ApplicationContext.
 *
 * @since 0.51.0
 *
 * @deprecated since 0.76.0, use edison-cache instead
 */
@Configuration
@EnableCaching
@Beta
@Deprecated
public class GuavaCacheConfiguration {

    @Autowired(required = false)
    List<GuavaCacheConfig> cacheConfigs;

    @Bean
    @ConditionalOnBean(GuavaCacheConfig.class)
    public CacheManager cacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(cacheConfigs
                .stream()
                .map(config-> new GuavaCache(config.cacheName, from(config.spec).build()))
                .collect(toList()));
        return simpleCacheManager;
    }

    @Bean
    public GuavaCacheStatisticsProvider guavaCacheCacheStatisticsProvider() {
        return new GuavaCacheStatisticsProvider() {
            @Override
            public CacheStatistics getCacheStatistics(CacheManager cacheManager,
                                                      GuavaCache cache) {
                return new EdisonGuavaCacheStatistics(cache);
            }
        };
    }

    class EdisonGuavaCacheStatistics implements CacheStatistics {
        private final long size;
        private final long requestCount;
        private double hitRatio;
        private double missRatio;
        private long hitCount;
        private long missCount;
        private long evictionCount;
        private long loadCount;

        public EdisonGuavaCacheStatistics(final GuavaCache cache) {
            size = cache.getNativeCache().size();
            final CacheStats guavaStats = cache.getNativeCache().stats();
            requestCount = guavaStats.requestCount();
            if (requestCount > 0) {
                hitRatio = guavaStats.hitRate();
                missRatio = guavaStats.missRate();
                loadCount = guavaStats.loadCount();
                evictionCount = guavaStats.evictionCount();
                hitCount = guavaStats.hitCount();
                missCount = guavaStats.missCount();
            }

        }

        @Override
        public Collection<Metric<?>> toMetrics(final String prefix) {
            Collection<Metric<?>> result = new ArrayList<>();
            addMetric(result, prefix + "size", size);
            addMetric(result, prefix + "eviction.count", evictionCount);
            addMetric(result, prefix + "load.count", loadCount);
            addMetric(result, prefix + "request.count", requestCount);
            addMetric(result, prefix + "hit.count", hitCount);
            addMetric(result, prefix + "hit.ratio", hitRatio);
            addMetric(result, prefix + "miss.count", missCount);
            addMetric(result, prefix + "miss.ratio", missRatio);
            return result;
        }

        @Override
        public Long getSize() {
            return this.size;
        }

        @Override
        public Double getHitRatio() {
            return this.hitRatio;
        }

        @Override
        public Double getMissRatio() {
            return this.missRatio;
        }

        private <T extends Number> void addMetric(Collection<Metric<?>> metrics, String name, T value) {
            if (value != null) {
                metrics.add(new Metric<T>(name, value));
            }
        }

    }
}
