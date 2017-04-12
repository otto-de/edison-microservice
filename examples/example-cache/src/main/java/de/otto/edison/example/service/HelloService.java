package de.otto.edison.example.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    @Cacheable("example-message")
    public String getMessage() {
        return "Hello Microservice";
    }

    /**
     * example-time is configured in {@link de.otto.edison.example.configuration.ExampleCacheConfiguration}
     * as a loading cache, so the implementation of this method is ignored.
     *
     * @return cached current time
     */
    @Cacheable("example-time")
    public String getTime() {
        return "";
    }
}
