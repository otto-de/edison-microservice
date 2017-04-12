package de.otto.edison.example.configuration;

import de.otto.edison.cache.configuration.CaffeineCacheConfig;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.benmanes.caffeine.cache.Caffeine.from;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@Configuration
public class ExampleCacheConfiguration {

    /**
     * Configures {@code example-message} cache using {@link CaffeineCacheConfig} from a cache specification.
     * <p>
     *     The cache is used by the HelloService using @Cacheable("example-message")
     * </p>
     *
     * @return CaffeineCacheConfig
     */
    @Bean
    CaffeineCacheConfig exampleCacheConfig() {
        return new CaffeineCacheConfig("example-message", "expireAfterWrite=10s,maximumSize=1,recordStats");
    }

    /**
     * Configures {@code example-time} cache as a loading cache that is expiring 1s after write.
     * <p>
     *     The cache is used by the HelloService using @Cacheable("example-time"). Alternatively,
     *     you could inject the CaffeineCache into a service or retrieve the Cache instance
     *     from the {@code cacheManger} bean.
     * </p>
     *
     * @return CaffeineCache
     */
    @Bean
    CaffeineCache exampleLoadingCacheConfig() {
        return new CaffeineCache(
                "example-time",
                from("expireAfterWrite=1s,maximumSize=1,recordStats")
                        .build(key -> ISO_LOCAL_DATE_TIME.format(now()))
        );
    }
}
