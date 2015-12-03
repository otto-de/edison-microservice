package de.otto.edison.cachestatistics;

import de.otto.edison.annotations.Beta;

@Beta
public class CacheStatistics {
    private final String name;
    private final Long maxCacheCapacity;
    private final Long cacheSize;
    private final Long cacheExpiresTimeInSeconds;

    private final Long hitCount;
    private final Long missCount;
    private final Long loadSuccessCount;
    private final Long loadExceptionCount;
    private final Long totalLoadTime;
    private final Long evictionCount;

    protected CacheStatistics(final String name,
                              final Long maxCacheCapacity,
                              final Long cacheSize,
                              final Long cacheExpiresTimeInSeconds,
                              final Long hitCount,
                              final Long missCount,
                              final Long loadSuccessCount,
                              final Long loadExceptionCount,
                              final Long totalLoadTime,
                              final Long evictionCount) {
        this.name = name;
        this.maxCacheCapacity = maxCacheCapacity;
        this.cacheSize = cacheSize;
        this.cacheExpiresTimeInSeconds = cacheExpiresTimeInSeconds;
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.loadSuccessCount = loadSuccessCount;
        this.loadExceptionCount = loadExceptionCount;
        this.totalLoadTime = totalLoadTime;
        this.evictionCount = evictionCount;
    }

    public double hitRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    public double missRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    public double loadExceptionRate() {
        long totalLoadCount = loadSuccessCount + loadExceptionCount;
        return (totalLoadCount == 0)
                ? 0.0
                : (double) loadExceptionCount / totalLoadCount;
    }

    public Long requestCount() {
        return hitCount + missCount;
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
        return hitCount;
    }

    public Long getMissCount() {
        return missCount;
    }

    public Long getLoadSuccessCount() {
        return loadSuccessCount;
    }

    public Long getLoadExceptionCount() {
        return loadExceptionCount;
    }

    public Long getTotalLoadTime() {
        return totalLoadTime;
    }

    public Long getEvictionCount() {
        return evictionCount;
    }

    public static Builder cacheStatisticsBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Long maxCacheCapacity;
        private Long cacheSize;
        private Long cacheExpiresTimeInSeconds;

        private Long hitCount;
        private Long missCount;
        private Long loadSuccessCount;
        private Long loadExceptionCount;
        private Long totalLoadTime;
        private Long evictionCount;

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

        public Builder withHitCount(Long hitCount) {
            this.hitCount = hitCount;
            return this;
        }

        public Builder withMissCount(Long missCount) {
            this.missCount = missCount;
            return this;
        }

        public Builder withLoadSuccessCount(Long loadSuccessCount) {
            this.loadSuccessCount = loadSuccessCount;
            return this;
        }

        public Builder withLoadExceptionCount(Long loadExceptionCount) {
            this.loadExceptionCount = loadExceptionCount;
            return this;
        }

        public Builder withTotalLoadTime(Long totalLoadTime) {
            this.totalLoadTime = totalLoadTime;
            return this;
        }

        public Builder withEvictionCount(Long evictionCount) {
            this.evictionCount = evictionCount;
            return this;
        }

        public CacheStatistics build() {
            return new CacheStatistics(name,
                    maxCacheCapacity,
                    cacheSize,
                    cacheExpiresTimeInSeconds,
                    hitCount,
                    missCount,
                    loadSuccessCount,
                    loadExceptionCount,
                    totalLoadTime,
                    evictionCount);
        }
    }
}

