package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.hystrix.http.HttpCommandBuilder.httpCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class HttpCommandBuilderTest {

    enum TestGroup implements HystrixCommandGroupKey {
        BAR
    }

    @Test
    public void shouldCreateSyncHttpCommand() throws IOException {
        // given
        // when
        final HystrixCommand<Response> command = httpCommand()
                .inGroup(TestGroup.BAR)
                .forRequest(mock(AsyncHttpClient.BoundRequestBuilder.class))
                .timingOutAfter(42, TimeUnit.DAYS)
                .build();
        // then
        assertThat(command.getClass().equals(HttpCommand.class), is(true));
    }

}