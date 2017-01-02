package de.otto.edison.cache.configuration;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.annotation.Cacheable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.benmanes.caffeine.cache.CaffeineSpec.parse;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Configuration of a {@link org.springframework.cache.caffeine.CaffeineCache Caffeine Cache} with a name and a specification string.
 *
 * CacheConfigs should be exposed as Spring Beans. All registered instances are collected by
 * {@link de.otto.edison.cache.configuration.CacheConfiguration} and used to register Caffeine caches accordingly.
 *
 * Spring Beans can use these caches by annotating a method as {@link org.springframework.cache.annotation.Cacheable},
 * with the {@link #cacheName cache name} as value.
 *
 * The metrics of caches are recorded, if they are configured with "recordStats". In this case, metrics will be
 * available under /internal/metrics, they will be reported according to the edison-metrics reporters, and they
 * will be accessible as HTML under /internal/cacheinfos.
 *
 * @since 0.76.0
 */
public final class CaffeineCacheConfig {

    /**
     * The name of the cache.
     *
     * @see Cacheable#cacheNames()
     */
    public final String cacheName;
    /**
     * The specification of the Caffeine {@link com.github.benmanes.caffeine.cache.Cache}. Used to construct
     * the cache in {@link de.otto.edison.cache.configuration.CacheConfiguration} and register it
     * in a {@link org.springframework.cache.CacheManager}, so it can be used using {@link Cacheable} annotations.
     */
    public final CaffeineSpec spec;



    /**
     * Create a CaffeineCacheConfig for a named cache, using a specification string to configure the cache.
     *
     * @param cacheName the name of the cache. You can use the configured cache using Spring's
     * {@link org.springframework.cache.annotation.Cacheable} annotation.
     * @param spec A specification of a Caffeine {@link CaffeineSpec } configuration.
     *
     * <p>Example: "initialCapacity=1,maximumSize=5,expireAfterAccess=10s,recordStats"</p>
     * <p>The string syntax is a series of comma-separated keys or key-value pairs,
     * each corresponding to a {@code CacheBuilder} method.
     * <ul>
     * <li>{@code initialCapacity=[integer]}: sets {@link CaffeineSpec#initialCapacity}.
     * <li>{@code maximumSize=[long]}: sets {@link CaffeineSpec#maximumSize}.
     * <li>{@code maximumWeight=[long]}: sets {@link CaffeineSpec#maximumWeight}.
     * <li>{@code expireAfterAccess=[duration]}: sets {@link CaffeineSpec#expireAfterAccess}.
     * <li>{@code expireAfterWrite=[duration]}: sets {@link CaffeineSpec#expireAfterWrite}.
     * <li>{@code refreshAfterWrite=[duration]}: sets {@link CaffeineSpec#refreshAfterWrite}.
     * <li>{@code weakKeys}: sets {@link CaffeineSpec#weakKeys}.
     * <li>{@code valueStrength}: sets {@link CaffeineSpec#valueStrength}.
     * <li>{@code recordStats}: sets {@link CaffeineSpec#recordStats}.
     * </ul>
     *
     * <p>Durations are represented by an integer, followed by one of "d", "h", "m",
     * or "s", representing days, hours, minutes, or seconds respectively.  (There
     * is currently no syntax to request expiration in milliseconds, microseconds,
     * or nanoseconds.)
     *
     * <p>Whitespace before and after commas and equal signs is ignored.  Keys may
     * not be repeated;  it is also illegal to use the following pairs of keys in
     * a single value:
     * <ul>
     * <li>{@code maximumSize} and {@code maximumWeight}
     * <li>{@code softValues} and {@code weakValues}
     * </ul>
     *
     * @since 0.76.0
     */
    public CaffeineCacheConfig(final String cacheName, final String spec) {
        this.cacheName = cacheName;
        this.spec = parse(spec);
    }

    /**
     *
     * @return Map containing the configuration keys and values of the {@link #spec cache specification}.
     */
    public Map<String,String> toMap() {
        final Map<String,String> map = new LinkedHashMap<>();
        for (final String keyValuePair : split(spec.toParsableString(), ",")) {
            final List<String> keyAndValue = split(keyValuePair, "=");
            if (keyAndValue.isEmpty()) throw new IllegalStateException("blank key-value pair");
            if (keyAndValue.size() > 2) throw new IllegalStateException(format("key-value pair %s with more than one equals sign", keyValuePair));
            map.put(keyAndValue.get(0), keyAndValue.size() == 1 ? "true" : keyAndValue.get(1));
        }
        return map;
    }

    private List<String> split(final String s, final String regex) {
        final String[] keyValues = s.split(regex);
        return keyValues != null ? asList(keyValues) : emptyList();
    }
}
