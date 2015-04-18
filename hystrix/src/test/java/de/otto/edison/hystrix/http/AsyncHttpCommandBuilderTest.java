package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.testng.annotations.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.hystrix.http.AsyncHttpCommandBuilder.asyncHttpCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class AsyncHttpCommandBuilderTest {

    enum TestGroup implements HystrixCommandGroupKey {
        FOO
    }

    @Test
    public void shouldCreateAsyncHttpCommand() {
        // given
        @SuppressWarnings("unchecked")
        final AsyncHandler<Future<Response>> handler = mock(AsyncHandler.class);
        // when
        final HystrixCommand<Future<Response>> command = asyncHttpCommand()
                .inGroup(TestGroup.FOO)
                .forRequest(mock(AsyncHttpClient.BoundRequestBuilder.class))
                .timingOutAfter(42, TimeUnit.DAYS)
                .withFallback(()->mock(Response.class))
                .handledBy(handler)
                .build();
        // then
        assertThat(command.getClass().equals(AsyncHttpCommand.class), is(true));
    }

}