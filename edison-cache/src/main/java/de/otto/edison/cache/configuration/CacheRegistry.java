package de.otto.edison.cache.configuration;


import com.github.benmanes.caffeine.cache.Cache;

/**
 */
public interface CacheRegistry {

    @SuppressWarnings("rawtypes")
    void registerCache(CaffeineCacheConfig config, Cache cache);
}
