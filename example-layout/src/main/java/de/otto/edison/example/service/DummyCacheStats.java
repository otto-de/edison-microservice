package de.otto.edison.example.service;

import de.otto.edison.cachestatistics.CacheStatistics;
import de.otto.edison.cachestatistics.CacheStatisticsProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static de.otto.edison.cachestatistics.CacheStatistics.cacheStatisticsBuilder;

@Component
public class DummyCacheStats implements CacheStatisticsProvider {

    @Override
    public List<CacheStatistics> getStats() {
        final List<CacheStatistics> cachingStats = new ArrayList<>();
        final CacheStatistics statForCache = cacheStatisticsBuilder()
                .withName("Example Cache One")
                .withMaxCacheCapacity(337L)
                .withCacheSize(37L)
                .withCacheExpiresTimeInSeconds(137L)
                .withHitCount(8L)
                .withMissCount(3L)
                .withLoadSuccessCount(2L)
                .withLoadExceptionCount(1L)
                .withTotalLoadTime(42L)
                .withEvictionCount(5L)
                .build();
        cachingStats.add(statForCache);

        final CacheStatistics statForCacheTwo = cacheStatisticsBuilder()
                .withName("Example Cache Two")
                .withMaxCacheCapacity(987L)
                .withCacheSize(44L)
                .withCacheExpiresTimeInSeconds(120L)
                .withHitCount(3L)
                .withMissCount(4L)
                .withLoadSuccessCount(4L)
                .withLoadExceptionCount(3L)
                .withTotalLoadTime(4L)
                .withEvictionCount(5L)
                .build();
        cachingStats.add(statForCacheTwo);

        return cachingStats;

    }
}


