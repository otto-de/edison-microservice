package de.otto.edison.cache.configuration;

import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.benmanes.caffeine.cache.Caffeine.from;

@Service
@ConditionalOnBean(CaffeineCacheConfig.class)
@Beta
public class DefaultCacheRegistry implements CacheManager, CacheRegistry {

    private List<CaffeineCacheConfig> cacheConfigs;
    private ConcurrentMap<String, Cache> caches;

    public DefaultCacheRegistry(@Autowired(required = false) List<CaffeineCacheConfig> cacheConfigs) {
        this.cacheConfigs = cacheConfigs;
        caches = new ConcurrentHashMap<>();
        this.cacheConfigs
                .stream()
                .map(config -> new CaffeineCache(config.cacheName, from(config.spec).build()))
                .forEach(cache -> caches.put(cache.getName(), cache));
    }

    @Override
    public void registerCache(CaffeineCacheConfig config, com.github.benmanes.caffeine.cache.Cache cache) {
        caches.put(config.cacheName, new CaffeineCache(config.cacheName, cache));
    }

    @Override
    public Cache getCache(String name) {
        return caches.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
