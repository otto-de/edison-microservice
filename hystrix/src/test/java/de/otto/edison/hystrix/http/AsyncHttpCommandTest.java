package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static de.otto.edison.hystrix.http.HttpCommands.newCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class AsyncHttpCommandTest {

    enum TestGroup implements HystrixCommandGroupKey {
        TEST
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldExecuteRequest() throws IOException {
        // given
        AsyncHandler asyncHandler = mock(AsyncHandler.class);
        // and
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute(asyncHandler)).thenReturn(mock(ListenableFuture.class));

        // when
        newCommand(TestGroup.TEST)
                .forRequest(mockRequest)
                .asyncUsing(asyncHandler)
                .execute();
        // then
        verify(mockRequest, atLeastOnce()).execute(anyObject());
    }

    @Test(expectedExceptions = HystrixRuntimeException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowExceptionWhenExecutionFails() throws IOException {
        // given
        AsyncHandler asyncHandler = mock(AsyncHandler.class);
        // and
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute(asyncHandler)).thenThrow(new IOException());

        // when
        newCommand(TestGroup.TEST)
                .forRequest(mockRequest)
                .asyncUsing(asyncHandler)
                .execute();
        // then an IOException is thrown.
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFallbackWhenExecutionFails() throws IOException, ExecutionException, InterruptedException {
        // given
        AsyncHandler<Future<Response>> asyncHandler = mock(AsyncHandler.class);
        // and
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute(asyncHandler)).thenThrow(new IOException());
        // and
        Response fallbackResponse = mock(Response.class);

        // when
        HystrixCommand<Future<Response>> hystrixCommand = newCommand(TestGroup.TEST)
                .forRequest(mockRequest)
                .withFallback(() -> fallbackResponse)
                .asyncUsing(asyncHandler);
        Future<Response> response = hystrixCommand.execute();
        // then
        assertThat(response.get(), is(fallbackResponse));
    }
}