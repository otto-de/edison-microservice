package de.otto.edison.example.service;

import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Timed;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

import static java.time.format.DateTimeFormatter.ofLocalizedTime;
import static java.time.format.FormatStyle.SHORT;

/**
 * Example service that is using caches configured in {@link de.otto.edison.example.configuration.CacheConfiguration}.
 * Beside of metrics exposed by the configured caches, this service is also using @Gauge and @Counted annotations
 * to expose some service-specific metrics.
 *
 * You can see these metrics by calling /example/internal/metrics. Cache metrics are also available as HTML under
 * /example/internal/cacheinfos.
 *
 * @author Guido Steinacker
 * @since 06.03.15
 */
@Service
public class HelloService {
    private static final Random random = new Random(42);

    /**
     * A method that is annotated with @Gauge
     *
     * @return some random value.
     */
    @Gauge(name = "HelloService.testGauge", absolute = true)
    public int testGauge() {
        return random.nextInt(42);
    }

    /**
     * A @Cacheable method that is returning a "Hello World"-like text
     *
     * The @Cacheable annotation is referring to the cache specification provided by
     * {@link de.otto.edison.example.configuration.CacheConfiguration} by name
     *
     * @param name some name
     * @return some message containing the name
     */
    @Cacheable("Hello Cache")
    public String getMessage(final String name) {
        try {
            Thread.sleep(10 * random.nextInt(100));
        } catch (InterruptedException e) {
            /* ignore */
        }
        return "Hello " + name;
    }

    /**
     * A @Cacheable method that is providing some random name.
     *
     * @return some name
     */
    @Timed(name = "HelloService.getName", absolute = true)
    public String getName() {
        try {
            Thread.sleep(10*random.nextInt(100));
        } catch (InterruptedException e) {
            /* ignore */
        }
        return "Edison " + ofLocalizedTime(SHORT).format(LocalDateTime.now());
    }
}
