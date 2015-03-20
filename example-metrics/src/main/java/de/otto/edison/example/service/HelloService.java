package de.otto.edison.example.service;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Gauge;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author Guido Steinacker
 * @since 06.03.15
 */
@Service
public class HelloService {

    private Random random = new Random(42);

    @Gauge(name = "HelloService.testGauge", absolute = true)
    public int testGauge() {
        return random.nextInt(42);
    }

    @Counted(name = "HelloService.getMessage", absolute = true, monotonic = true)
    public String getMessage() {
        return "Hello Microservice!";
    }
}
