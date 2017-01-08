package de.otto.edison.cache.configuration;

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

    @Bean
    @ConditionalOnBean(CaffeineCacheConfig.class)
    public CacheManager cacheManager(final List<CaffeineCacheConfig> cacheConfigs) {
        return new DefaultCacheRegistry(cacheConfigs);
    }

    @Bean
    public CaffeineCacheStatisticsProvider caffeineCacheCacheStatisticsProvider() {
        return new CaffeineCacheStatisticsProvider() {
            @Override
            public CacheStatistics getCacheStatistics(CacheManager cacheManager,
                                                      CaffeineCache cache) {
                return new CaffeineCacheStatistics(cache);
            }
        };
    }

}
