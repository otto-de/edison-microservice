package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * A Hystrix command that is asynchronously getting a resource using AsyncHttpClient.
 *
 * @author Guido Steinacker
 * @since 15.04.15
 */
final class AsyncHttpCommand extends HystrixCommand<Future<Response>> {

    private final AsyncHttpClient.BoundRequestBuilder requestBuilder;
    private final AsyncHandler<Future<Response>> asyncHandler;
    private final int timeout;
    private final TimeUnit timeUnit;
    private final Optional<Supplier<Response>> fallback;

    AsyncHttpCommand(final Setter setter,
                            final AsyncHandler<Future<Response>> asyncHandler,
                            final AsyncHttpClient.BoundRequestBuilder requestBuilder,
                            final Optional<Supplier<Response>> fallback,
                            final int timeout,
                            final TimeUnit timeUnit) {
        super(setter);
        this.requestBuilder = requestBuilder;
        this.asyncHandler = asyncHandler;
        this.fallback = fallback;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    protected Future<Response> run() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return requestBuilder.execute(asyncHandler).get(timeout, timeUnit);
    }

    @Override
    protected Future<Response> getFallback() {
        if (fallback.isPresent()) {
            return completedFuture(fallback.get().get());
        } else {
            return super.getFallback();
        }
    }
}
