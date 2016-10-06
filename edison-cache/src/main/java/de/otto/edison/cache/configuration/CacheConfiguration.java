package de.otto.edison.cache.configuration;

import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.CaffeineCacheStatisticsProvider;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.github.benmanes.caffeine.cache.Caffeine.from;
import static java.util.stream.Collectors.toList;

/**
 * Configuration that is exposing a CacheManager and cache statistics for Caffeine caches.
 *
 * The CacheManager is configured with caches for all {@link CaffeineCacheConfig} beans found in
 * the ApplicationContext.
 *
 * @since 0.76.0
 *
 *
 */
@Configuration
@EnableCaching
@Beta
public class CacheConfiguration {

    @Autowired(required = false)
    List<CaffeineCacheConfig> cacheConfigs;

    @Bean
    @ConditionalOnBean(CaffeineCacheConfig.class)
    public CacheManager cacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(cacheConfigs
                .stream()
                .map(config-> new CaffeineCache(config.cacheName, from(config.spec).build()))
                .collect(toList()));
        return simpleCacheManager;
    }

    @Bean
    public CaffeineCacheStatisticsProvider caffeineCacheCacheStatisticsProvider() {
        return new CaffeineCacheStatisticsProvider() {
            @Override
            public CacheStatistics getCacheStatistics(CacheManager cacheManager,
                                                      CaffeineCache cache) {
                return new EdisonCaffeineCacheStatistics(cache);
            }
        };
    }

    class EdisonCaffeineCacheStatistics implements CacheStatistics {
        private final long size;
        private final long requestCount;
        private double hitRatio;
        private double missRatio;
        private long hitCount;
        private long missCount;
        private long evictionCount;
        private long loadCount;

        public EdisonCaffeineCacheStatistics(final CaffeineCache cache) {
            size = cache.getNativeCache().estimatedSize();
            final com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = cache.getNativeCache().stats();
            requestCount = caffeineStats.requestCount();
            if (requestCount > 0) {
                hitRatio = caffeineStats.hitRate();
                missRatio = caffeineStats.missRate();
                loadCount = caffeineStats.loadCount();
                evictionCount = caffeineStats.evictionCount();
                hitCount = caffeineStats.hitCount();
                missCount = caffeineStats.missCount();
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
