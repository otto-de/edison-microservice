package de.otto.edison.example.service;

import com.google.common.cache.CacheStats;
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
                .withGuavaCacheStats(new CacheStats(8L, 3L, 2L, 1L, 42L, 5L))
                .build();
        cachingStats.add(statForCache);

        final CacheStatistics statForCacheTwo = cacheStatisticsBuilder()
                .withName("Example Cache Two")
                .withMaxCacheCapacity(987L)
                .withCacheSize(44L)
                .withCacheExpiresTimeInSeconds(120L)
                .withGuavaCacheStats(new CacheStats(3L, 4L, 4L, 3L, 4L, 5L))
                .build();
        cachingStats.add(statForCacheTwo);

        return cachingStats;

    }
}


