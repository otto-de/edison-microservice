package de.otto.edison.cache.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.CaffeineCacheStatisticsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration that is exposing a CacheManager and cache statistics for Caffeine caches.
 * <p>
 *     The CacheManager is configured with caches for all {@link CaffeineCacheConfig} beans found in
 *     the ApplicationContext.
 * </p>
 *
 * @since 0.76.0
 *
 *
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Autowired(required = false)
    private List<CaffeineCacheConfig> cacheConfigs;
    @Autowired(required = false)
    private List<CaffeineCache>  caffeineCaches;

    @Bean
    public CacheManager cacheManager() {
        return new EdisonCacheManager(cacheConfigs, caffeineCaches);
    }

    @Bean
    public CaffeineCacheStatisticsProvider caffeineCacheCacheStatisticsProvider() {
        return new CaffeineCacheStatisticsProvider() {
            @Override
            public CacheStatistics getCacheStatistics(final CacheManager cacheManager,
                                                      final CaffeineCache cache) {
                return new CaffeineCacheStatistics(cache);
            }
        };
    }

}
