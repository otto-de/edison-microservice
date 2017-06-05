package de.otto.edison.metrics.http;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpMetricsFilterTest {

    @Test
    public void shouldCountRequestsAndMeasureResponseTimes() throws IOException, ServletException {
        // given a Counter
        final Counter counter = mock(Counter.class);
        // and a Timer and Context
        final Timer timer = mock(Timer.class);
        final Timer.Context context = mock(Timer.Context.class);
        // returned by a MetricRegistry
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(anyString())).thenReturn(counter);
        when(metricRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(context);
        // and some GET request
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo/bar");
        // and a HTTP 200 response
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        // when
        new HttpMetricsFilter(metricRegistry).doFilter(request, response, mock(FilterChain.class));

        // then
        verify(metricRegistry).counter("counter.http.get.200");
        verify(counter).inc();
        verify(metricRegistry).timer("timer.http.get");
        verify(context).stop();
    }

    @Test
    public void shouldProceedInFilterChain() throws IOException, ServletException {

        // given a MetricRegistry
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(anyString())).thenReturn(mock(Counter.class));
        // and a Timer and Context
        final Timer timer = mock(Timer.class);
        final Timer.Context context = mock(Timer.Context.class);
        when(metricRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(context);
        // and a FilterChain
        final FilterChain filterChain = mock(FilterChain.class);
        // and a GET request
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        // and some response object
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        new HttpMetricsFilter(metricRegistry).doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

}
