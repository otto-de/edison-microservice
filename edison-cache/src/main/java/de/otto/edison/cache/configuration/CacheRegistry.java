package de.otto.edison.cache.configuration;


import com.github.benmanes.caffeine.cache.Cache;

/**
 * @Beta
 */
public interface CacheRegistry {

    void registerCache(CaffeineCacheConfig config, Cache cache);
}
