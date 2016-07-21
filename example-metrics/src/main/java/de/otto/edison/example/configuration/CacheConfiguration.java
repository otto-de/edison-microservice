package de.otto.edison.example.configuration;

import de.otto.edison.cache.configuration.GuavaCacheConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Example for some cache configurations that are automatically used by edison-guava to configure Guava caches
 * accordingly.
 *
 * The format of the cache specifications is described in {@link GuavaCacheConfig}.
 *
 * These caches can be referred by name. An example for this can be found in
 * {@link de.otto.edison.example.service.HelloService}
 *
 * @since 0.51.0
 */
@Configuration
public class CacheConfiguration {

    @Bean
    public GuavaCacheConfig helloCacheConfig() {
        return new GuavaCacheConfig(
                "Hello Cache",
                "initialCapacity=1,maximumSize=5,expireAfterAccess=3s,expireAfterWrite=5s,recordStats"
        );
    }

}
