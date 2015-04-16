package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Hystrix command that is asynchronously getting a resource using AsyncHttpClient.
 *
 * @author Guido Steinacker
 * @since 15.04.15
 */
public final class AsyncHttpCommand<T> extends HystrixCommand<T> {

    private final AsyncHttpClient.BoundRequestBuilder requestBuilder;
    private final AsyncHandler<T> asyncHandler;
    private final int timeout;
    private final TimeUnit timeUnit;


    public AsyncHttpCommand(final HystrixCommandGroupKey commandGroup,
                            final AsyncHandler<T> asyncHandler,
                            final AsyncHttpClient.BoundRequestBuilder requestBuilder,
                            final int timeout,
                            final TimeUnit timeUnit) {
        super(commandGroup);
        this.requestBuilder = requestBuilder;
        this.asyncHandler = asyncHandler;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    protected T run() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return requestBuilder.execute(asyncHandler).get(timeout, timeUnit);
    }


}
