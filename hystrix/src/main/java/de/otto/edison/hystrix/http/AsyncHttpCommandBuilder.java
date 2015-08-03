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
import static java.util.Optional.ofNullable;

/**
 * A fluent builder used to create and configure http commands.
 *
 * @author Guido Steinacker
 * @since 16.04.15
 */
public final class AsyncHttpCommandBuilder<T> {

    private AsyncHandler<Future<T>> handler;
    protected HystrixCommand.Setter setter;
    protected AsyncHttpClient.BoundRequestBuilder requestBuilder;
    protected Optional<Supplier<T>> optionalFallback = Optional.empty();
    protected int timeout = 10;
    protected TimeUnit timeUnit = TimeUnit.SECONDS;

    public static AsyncHttpCommandBuilder<Response> asyncHttpCommand() {
        return new AsyncHttpCommandBuilder<Response>();
    }

    public static <T> AsyncHttpCommandBuilder<T> asyncHttpCommand(final Class<T> type) {
        return new AsyncHttpCommandBuilder<T>();
    }

    public AsyncHttpCommandBuilder<T> inGroup(final HystrixCommandGroupKey group) {
        this.setter = withGroupKey(group);
        return this;
    }

    public AsyncHttpCommandBuilder<T> configuredBy(final HystrixCommand.Setter setter) {
        this.setter = setter;
        return this;
    }

    public AsyncHttpCommandBuilder<T> forRequest(final AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        this.requestBuilder = requireNonNull(requestBuilder);
        return this;
    }

    public AsyncHttpCommandBuilder<T> withFallback(final Supplier<T> fallback) {
        this.optionalFallback = ofNullable(fallback);
        return this;
    }

    public AsyncHttpCommandBuilder<T> timingOutAfter(final int timeout, final TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = requireNonNull(timeUnit);
        return this;
    }

    public AsyncHttpCommandBuilder<T> handledBy(final AsyncHandler<Future<T>> handler) {
        this.handler = handler;
        return this;
    }

    public HystrixCommand<Future<T>> build() {
        return new AsyncHttpCommand<>(
                setter,
                handler,
                requestBuilder,
                optionalFallback,
                timeout, timeUnit);
    }

    AsyncHttpCommandBuilder() {
    }
}
