package de.otto.edison.metrics.http;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetricsFilterTest {

    @Test
    public void shouldCountRequests() throws IOException, ServletException {
        // given a Counter
        final Counter counter = mock(Counter.class);
        // returned by a MetricRegistry
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(anyString())).thenReturn(counter);
        // and some GET request
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo/bar");
        // and a HTTP 200 response
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        // when
        new MetricsFilter(metricRegistry).doFilter(request, response, mock(FilterChain.class));

        // then
        verify(metricRegistry).counter("counter.http.get.200");
        verify(counter).inc();
    }

    @Test
    public void shouldProceedInFilterChain() throws IOException, ServletException {

        // given a MetricRegistry
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(anyString())).thenReturn(mock(Counter.class));
        // and a FilterChain
        final FilterChain filterChain = mock(FilterChain.class);
        // and a GET request
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        // and some response object
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        new MetricsFilter(metricRegistry).doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

}