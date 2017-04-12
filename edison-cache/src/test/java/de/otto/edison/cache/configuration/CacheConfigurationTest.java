package de.otto.edison.cache.configuration;

import de.otto.edison.cache.CaffeineCacheConfigTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class CacheConfigurationTest {


    private AnnotationConfigApplicationContext context;

    @Before
    public void open() {
        context = new AnnotationConfigApplicationContext();
    }

    @After
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void shouldExposeCacheManager() {
        context.register(CacheConfigTestConfiguration.class);
        context.register(CacheConfiguration.class);
        context.refresh();

        CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);

        assertThat(cacheManager, is(notNullValue()));
    }

    @Configuration
    static class CacheConfigTestConfiguration {
        @Bean CaffeineCacheConfig someConfig() {
            return new CaffeineCacheConfig("test", "");
        }
    }
}