package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
public final class HttpCommandBuilder<T> {
    private Function<Response, T> responseMapper = null;
    protected HystrixCommand.Setter setter;
    protected AsyncHttpClient.BoundRequestBuilder requestBuilder;
    protected Optional<Supplier<T>> optionalFallback = Optional.empty();
    protected int timeout = 10;
    protected TimeUnit timeUnit = TimeUnit.SECONDS;

    public static HttpCommandBuilder<Response> httpCommand() {
        return new HttpCommandBuilder<Response>().mappedBy((r)->r);
    }

    public static <T> HttpCommandBuilder<T> httpCommand(final Class<T> type) {
        return new HttpCommandBuilder<T>();
    }

    public HttpCommandBuilder<T> inGroup(final HystrixCommandGroupKey group) {
        this.setter = withGroupKey(group);
        return this;
    }

    public HttpCommandBuilder<T> inGroup(final String hystrixCommandGgroup) {
        this.setter = withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixCommandGgroup));
        return this;
    }

    public HttpCommandBuilder<T> configuredBy(final HystrixCommand.Setter setter) {
        this.setter = setter;
        return this;
    }

    public HttpCommandBuilder<T> forRequest(final AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        this.requestBuilder = requireNonNull(requestBuilder);
        return this;
    }

    public HttpCommandBuilder<T> withFallback(final Supplier<T> fallback) {
        this.optionalFallback = ofNullable(fallback);
        return this;
    }

    public HttpCommandBuilder<T> timingOutAfter(final int timeout, final TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = requireNonNull(timeUnit);
        return this;
    }
    public HttpCommandBuilder<T> mappedBy(final Function<Response, T> responseMapper) {
        this.responseMapper = responseMapper;
        return this;
    }

    public HystrixCommand<T> build() {
        return new HttpCommand<T>(
                requireNonNull(setter, "Missing Setter"),
                requireNonNull(requestBuilder, "Missing request builder"),
                optionalFallback,
                requireNonNull(responseMapper, "Missing response mapper"),
                timeout, timeUnit);
    }

    HttpCommandBuilder() {
    }
}
