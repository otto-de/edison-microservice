package de.otto.edison.cache.configuration;


import com.github.benmanes.caffeine.cache.Cache;

/**
 * @deprecated will be removed in edison 2.0.0
 */
@Deprecated
public interface CacheRegistry {

    @SuppressWarnings("rawtypes")
    void registerCache(CaffeineCacheConfig config, Cache cache);
}
