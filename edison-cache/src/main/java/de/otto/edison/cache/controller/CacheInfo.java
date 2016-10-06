package de.otto.edison.cache.controller;

import de.otto.edison.annotations.Beta;
import de.otto.edison.cache.configuration.CaffeineCacheConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Statistics and specification of configured Caffeine caches.
 *
 * @since 0.76.0
 */
@Beta
public final class CacheInfo {

    private final String name;
    private final Map<String,Number> metrics = new LinkedHashMap<>();
    private final Map<String,String> specification = new LinkedHashMap<>();

    /**
     * Creates a CacheInfo object for the specified cache.
     *
     * @param cacheName the name of the cache as specified in {@link CaffeineCacheConfig}.
     */
    public CacheInfo(final String cacheName) {
        this.name = cacheName;
    }

    /**
     * Returns the name of the cache.
     *
     * @return the name of the cache as specified in {@link CaffeineCacheConfig}.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a single metric value.
     *
     * @param key the key of the metric
     * @param value the metric's value
     */
    void setMetric(final String key, final Number value) {
        metrics.put(key, value);
    }

    /**
     * Returns the Map of metrics.
     *
     * @return Current values of the cache metrics.
     */
    public Map<String, Number> getMetrics() {
        return metrics;
    }

    /**
     * Sets the cache specification containing information about expiration etc.
     *
     * @param spec Cache specification
     */
    void setSpecification(final Map<String,String> spec) {
        this.specification.putAll(spec);
    }

    /**
     * Returns the cache specification properties.
     *
     * @return Cache specification
     */
    public Map<String, String> getSpecification() {
        return specification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheInfo cacheInfo = (CacheInfo) o;

        if (name != null ? !name.equals(cacheInfo.name) : cacheInfo.name != null) return false;
        if (metrics != null ? !metrics.equals(cacheInfo.metrics) : cacheInfo.metrics != null) return false;
        return !(specification != null ? !specification.equals(cacheInfo.specification) : cacheInfo.specification != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (metrics != null ? metrics.hashCode() : 0);
        result = 31 * result + (specification != null ? specification.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "name='" + name + '\'' +
                ", metrics=" + metrics +
                ", specification=" + specification +
                '}';
    }
}
