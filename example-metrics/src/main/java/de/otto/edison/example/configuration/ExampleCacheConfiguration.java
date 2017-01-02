package de.otto.edison.example.configuration;

import de.otto.edison.cache.configuration.CaffeineCacheConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Example for some cache configurations that are automatically used by edison-cache to configure Caffeine caches
 * accordingly.
 *
 * The format of the cache specifications is described in {@link CaffeineCacheConfig}.
 *
 * These caches can be referred by name. An example for this can be found in
 * {@link de.otto.edison.example.service.HelloService}
 *
 * @since 0.51.0
 */
@Configuration
public class ExampleCacheConfiguration {

    @Bean
    public CaffeineCacheConfig helloCacheConfig() {
        return new CaffeineCacheConfig(
                "Hello Cache",
                "initialCapacity=1,maximumSize=5,expireAfterAccess=3s,expireAfterWrite=5s,recordStats"
        );
    }

}
