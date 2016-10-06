package de.otto.edison.cache.configuration;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import de.otto.edison.annotations.Beta;
import org.springframework.cache.annotation.Cacheable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.cache.CacheBuilderSpec.parse;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Configuration of a {@link com.google.common.cache.Cache Guava Cache} with a name and a specification string.
 *
 * GuavaCacheConfigs should be exposed as Spring Beans. All registered instances are collected by
 * {@link de.otto.edison.cache.configuration.GuavaCacheConfiguration} and used to register Guava caches accordingly.
 *
 * Spring Beans can use these caches by annotating a method as {@link org.springframework.cache.annotation.Cacheable},
 * with the {@link #cacheName cache name} as value.
 *
 * The metrics of caches are recorded, if they are configured with "recordStats". In this case, metrics will be
 * available under /internal/metrics, they will be reported according to the edison-metrics reporters, and they
 * will be accessible as HTML under /internal/cacheinfos.
 *
 * @deprecated since 0.76.0, use {@link de.otto.edison.cache.configuration.CaffeineCacheConfig} from edison-cache instead
 *
 * @since 0.51.0
 */
@Beta
@Deprecated
public final class GuavaCacheConfig {

    private static final Splitter KEYS_SPLITTER = Splitter.on(',').trimResults();

    /** Splits the key from the value. */
    private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').trimResults();

    /**
     * The name of the cache.
     *
     * @see Cacheable#cacheNames()
     */
    public final String cacheName;
    /**
     * The specification of the Guava {@link com.google.common.cache.Cache}. Used to construct
     * the cache in {@link de.otto.edison.cache.configuration.GuavaCacheConfiguration} and register it
     * in a {@link org.springframework.cache.CacheManager}, so it can be used using {@link Cacheable} annotations.
     */
    public final CacheBuilderSpec spec;

    /**
     * Create a GuavaCacheConfig for a named cache, using a specification string to configure the cache.
     *
     * @param cacheName the name of the cache. You can use the configured cache using Spring's
     * {@link org.springframework.cache.annotation.Cacheable} annotation.
     * @param spec A specification of a Guava {@link CacheBuilder} configuration.
     *
     * <p>Example: "initialCapacity=1,maximumSize=5,expireAfterAccess=10s,recordStats"</p>
     * <p>The string syntax is a series of comma-separated keys or key-value pairs,
     * each corresponding to a {@code CacheBuilder} method.
     * <ul>
     * <li>{@code concurrencyLevel=[integer]}: sets {@link CacheBuilder#concurrencyLevel}.
     * <li>{@code initialCapacity=[integer]}: sets {@link CacheBuilder#initialCapacity}.
     * <li>{@code maximumSize=[long]}: sets {@link CacheBuilder#maximumSize}.
     * <li>{@code maximumWeight=[long]}: sets {@link CacheBuilder#maximumWeight}.
     * <li>{@code expireAfterAccess=[duration]}: sets {@link CacheBuilder#expireAfterAccess}.
     * <li>{@code expireAfterWrite=[duration]}: sets {@link CacheBuilder#expireAfterWrite}.
     * <li>{@code refreshAfterWrite=[duration]}: sets {@link CacheBuilder#refreshAfterWrite}.
     * <li>{@code weakKeys}: sets {@link CacheBuilder#weakKeys}.
     * <li>{@code softValues}: sets {@link CacheBuilder#softValues}.
     * <li>{@code weakValues}: sets {@link CacheBuilder#weakValues}.
     * <li>{@code recordStats}: sets {@link CacheBuilder#recordStats}.
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
     * @since 0.51.0
     */
    public GuavaCacheConfig(final String cacheName, final String spec) {
        this.cacheName = cacheName;
        this.spec = parse(spec);
    }

    /**
     *
     * @return Map containing the configuration keys and values of the {@link #spec cache specification}.
     */
    public Map<String,String> toMap() {
        final Map<String,String> map = new LinkedHashMap<>();
        for (final String keyValuePair : KEYS_SPLITTER.split(spec.toParsableString())) {
            final List<String> keyAndValue = copyOf(KEY_VALUE_SPLITTER.split(keyValuePair));
            checkArgument(!keyAndValue.isEmpty(), "blank key-value pair");
            checkArgument(keyAndValue.size() <= 2, "key-value pair %s with more than one equals sign", keyValuePair);
            map.put(keyAndValue.get(0), keyAndValue.size() == 1 ? "true" : keyAndValue.get(1));
        }
        return map;
    }
}
