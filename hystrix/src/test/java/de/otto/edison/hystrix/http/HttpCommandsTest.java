package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static de.otto.edison.hystrix.http.HttpCommands.newCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

public class HttpCommandsTest {

    enum TestGroup implements HystrixCommandGroupKey {
        FOO, BAR
    }

    @Test
    public void shouldCreateAsyncHttpCommand() {
        // given
        @SuppressWarnings("unchecked")
        final AsyncHandler<Integer> handler = mock(AsyncHandler.class);
        // when
        final HystrixCommand<Integer> command = newCommand(TestGroup.FOO)
                .forRequest(mock(AsyncHttpClient.BoundRequestBuilder.class))
                .timingOutAfter(42, TimeUnit.DAYS)
                .asyncUsing(handler);
        // then
        assertThat(command.getClass().equals(AsyncHttpCommand.class), is(true));
    }

    @Test
    public void shouldCreateSyncHttpCommand() {
        // given
        // when
        final HystrixCommand<Response> command = newCommand(TestGroup.BAR)
                .forRequest(mock(AsyncHttpClient.BoundRequestBuilder.class))
                .timingOutAfter(42, TimeUnit.DAYS)
                .sync();
        // then
        assertThat(command.getClass().equals(HttpCommand.class), is(true));
    }

}