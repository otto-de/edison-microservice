package de.otto.edison.cachestatistics;

import com.google.common.cache.CacheStats;
import de.otto.edison.annotations.Beta;

@Beta
public class CacheStatistics {
    private final String name;
    private final Long maxCacheCapacity;
    private final Long cacheSize;
    private final Long cacheExpiresTimeInSeconds;

    private final CacheStats cacheStats;

    protected CacheStatistics(final String name,
                              final Long maxCacheCapacity,
                              final Long cacheSize,
                              final Long cacheExpiresTimeInSeconds,
                              final CacheStats cacheStats) {
        this.name = name;
        this.maxCacheCapacity = maxCacheCapacity;
        this.cacheSize = cacheSize;
        this.cacheExpiresTimeInSeconds = cacheExpiresTimeInSeconds;
        this.cacheStats = cacheStats;
    }

    public double getHitRate() {
        return cacheStats.hitRate();
    }

    public double getMissRate() {
        return cacheStats.missRate();
    }

    public double getLoadExceptionRate() {
        return cacheStats.loadExceptionRate();
    }

    public Long getRequestCount() {
        return cacheStats.requestCount();
    }

    public String getName() {
        return name;
    }

    public Long getMaxCacheCapacity() {
        return maxCacheCapacity;
    }

    public Long getCacheSize() {
        return cacheSize;
    }

    public Long getCacheExpiresTimeInSeconds() {
        return cacheExpiresTimeInSeconds;
    }

    public Long getHitCount() {
        return cacheStats.hitCount();
    }

    public Long getMissCount() {
        return cacheStats.missCount();
    }

    public Long getLoadSuccessCount() {
        return cacheStats.loadSuccessCount();
    }

    public Long getLoadExceptionCount() {
        return cacheStats.loadExceptionCount();
    }

    public Long getTotalLoadTime() {
        return cacheStats.totalLoadTime();
    }

    public Long getEvictionCount() {
        return cacheStats.evictionCount();
    }

    public static Builder cacheStatisticsBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Long maxCacheCapacity;
        private Long cacheSize;
        private Long cacheExpiresTimeInSeconds;
        private CacheStats cacheStats;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withMaxCacheCapacity(Long maxCacheCapacity) {
            this.maxCacheCapacity = maxCacheCapacity;
            return this;
        }

        public Builder withCacheSize(Long cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder withCacheExpiresTimeInSeconds(Long cacheExpiresTimeInSeconds) {
            this.cacheExpiresTimeInSeconds = cacheExpiresTimeInSeconds;
            return this;
        }

        public Builder withGuavaCacheStats(CacheStats cacheStats) {
            this.cacheStats = cacheStats;
            return this;
        }

        public CacheStatistics build() {
            return new CacheStatistics(name,
                    maxCacheCapacity,
                    cacheSize,
                    cacheExpiresTimeInSeconds,
                    cacheStats);
        }
    }
}

