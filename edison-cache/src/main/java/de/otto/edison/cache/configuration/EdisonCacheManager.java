package de.otto.edison.cache.configuration;

import de.otto.edison.annotations.Beta;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.benmanes.caffeine.cache.Caffeine.from;

@Beta
public class EdisonCacheManager implements CacheManager {

    private ConcurrentMap<String, Cache> caches;

    public EdisonCacheManager(final List<CaffeineCacheConfig> cacheConfigs,
                              final List<CaffeineCache> caffeineCaches) {
        caches = new ConcurrentHashMap<>();
        if (cacheConfigs != null) {
            cacheConfigs
                    .stream()
                    .map(config -> new CaffeineCache(config.cacheName, from(config.spec).build()))
                    .forEach(cache -> caches.put(cache.getName(), cache));
        }
        if (caffeineCaches != null) {
            caffeineCaches
                    .forEach(cache -> caches.put(cache.getName(), cache));
        }
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
