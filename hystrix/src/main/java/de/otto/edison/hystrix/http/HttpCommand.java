package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Hystrix command that is synchronously getting a resource using AsyncHttpClient.
 *
 * @author Guido Steinacker
 * @since 15.04.15
 */
final class HttpCommand<T> extends HystrixCommand<T> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpCommand.class);

    private final AsyncHttpClient.BoundRequestBuilder requestBuilder;
    private final Optional<Supplier<T>> fallback;
    private final Function<Response, T> mapper;
    private final int timeout;
    private final TimeUnit timeUnit;

    HttpCommand(final Setter setter,
                final AsyncHttpClient.BoundRequestBuilder requestBuilder,
                final Optional<Supplier<T>> fallback,
                final Function<Response, T> mapper,
                final int timeout,
                final TimeUnit timeUnit) {
        super(setter);
        this.requestBuilder = requestBuilder;
        this.fallback = fallback;
        this.mapper = mapper;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    protected T run() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        try {
            return mapper.apply(
                    requestBuilder.execute().get(timeout, timeUnit)
            );
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected T getFallback() {
        return fallback.orElse(() -> super.getFallback()).get();
    }
}
