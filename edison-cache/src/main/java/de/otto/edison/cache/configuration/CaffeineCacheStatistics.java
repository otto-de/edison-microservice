package de.otto.edison.cache.configuration;

import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CacheStatistics for Caffeine caches.
 */
class CaffeineCacheStatistics implements CacheStatistics {
    private final long size;
    private final long requestCount;
    private double hitRatio;
    private double missRatio;
    private long hitCount;
    private long missCount;
    private long evictionCount;
    private long loadCount;

    public CaffeineCacheStatistics(final CaffeineCache cache) {
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
