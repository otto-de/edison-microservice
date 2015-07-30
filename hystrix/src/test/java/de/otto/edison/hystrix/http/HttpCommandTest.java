package de.otto.edison.hystrix.http;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.edison.hystrix.http.HttpCommandBuilder.httpCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class HttpCommandTest {

    enum TestGroup implements HystrixCommandGroupKey {
        TEST
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldExecuteRequest() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenReturn(mock(ListenableFuture.class));

        // when
        httpCommand()
                .inGroup(TestGroup.TEST)
                .forRequest(mockRequest)
                .build()
                .execute();
        // then
        verify(mockRequest, atLeastOnce()).execute();
    }

    @Test(expectedExceptions = HystrixRuntimeException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowExceptionWhenExecutionFails() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenThrow(new RuntimeException());

        // when
        httpCommand()
                .inGroup(TestGroup.TEST)
                .forRequest(mockRequest)
                .build()
                .execute();
        // then an HystrixRuntimeException is thrown.
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFallbackWhenExecutionFails() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenThrow(new RuntimeException());
        // and
        Response fallbackResponse = mock(Response.class);

        // when
        final Response response = httpCommand()
                .inGroup(TestGroup.TEST)
                .forRequest(mockRequest)
                .withFallback(() -> fallbackResponse)
                .build()
                .execute();
        // then
        assertThat(response, is(fallbackResponse));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFallbackToStringWhenExecutionFails() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenThrow(new RuntimeException());
        // and
        String fallbackResponse = "42";

        // when
        final String response = httpCommand(String.class)
                .inGroup(TestGroup.TEST)
                .forRequest(mockRequest)
                .mappedBy(x -> "")
                .withFallback(() -> fallbackResponse)
                .build()
                .execute();
        // then
        assertThat(response, is(fallbackResponse));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFallbackToStringWhenMappingFails() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenReturn(mock(ListenableFuture.class));
        // and
        String fallbackResponse = "42";

        // when
        final String response = httpCommand(String.class)
                .inGroup(TestGroup.TEST)
                .forRequest(mockRequest)
                .mappedBy(x -> {
                    throw new RuntimeException();
                })
                .withFallback(() -> fallbackResponse)
                .build()
                .execute();
        // then
        assertThat(response, is(fallbackResponse));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMapResponse() throws IOException {
        // given
        AsyncHttpClient.BoundRequestBuilder mockRequest = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(mockRequest.execute()).thenReturn(mock(ListenableFuture.class));

        // when
        String response = httpCommand(String.class)
                .inGroup("Foo")
                .forRequest(mockRequest)
                .mappedBy(x -> "42")
                .build()
                .execute();
        // then
        assertThat(response, is("42"));
    }
}