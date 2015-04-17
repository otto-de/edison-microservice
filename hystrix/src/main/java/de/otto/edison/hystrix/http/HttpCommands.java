package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static java.util.Objects.requireNonNull;

/**
 * A fluent builder used to create and configure http commands.
 *
 * @author Guido Steinacker
 * @since 16.04.15
 */
public final class HttpCommands {
    private HystrixCommand.Setter setter;
    private AsyncHttpClient.BoundRequestBuilder requestBuilder;
    private Optional<Supplier<Response>> optionalFallback = Optional.empty();
    private int timeout = 10;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public static HttpCommands newCommand(final HystrixCommandGroupKey key) {
        return new HttpCommands(withGroupKey(key));
    }

    public static HttpCommands newCommand(final HystrixCommand.Setter setter) {
        return new HttpCommands(setter);
    }

    public HttpCommands forRequest(final AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        this.requestBuilder = requireNonNull(requestBuilder);
        return this;
    }

    public HttpCommands withFallback(final Supplier<Response> fallback) {
        this.optionalFallback = Optional.ofNullable(fallback);
        return this;
    }

    public HttpCommands timingOutAfter(final int timeout, final TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = requireNonNull(timeUnit);
        return this;
    }

    public HystrixCommand<Response> sync() {
        return new HttpCommand(
                setter,
                requireNonNull(requestBuilder, "Missing request builder"),
                optionalFallback,
                timeout, timeUnit);
    }

    public HystrixCommand<Future<Response>> asyncUsing(final AsyncHandler<Future<Response>> handler) {
        return new AsyncHttpCommand(
                setter,
                requireNonNull(handler),
                requestBuilder,
                optionalFallback,
                timeout, timeUnit);
    }

    private HttpCommands(HystrixCommand.Setter setter) {
        this.setter = requireNonNull(setter);
    }
}
