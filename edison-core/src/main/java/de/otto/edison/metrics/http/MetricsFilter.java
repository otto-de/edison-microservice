package de.otto.edison.metrics.http;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter that records some metrics:
 * - Counting http requests for different methods and status
 * - Measuring response rates and times for different methods
 *
 * Created by guido on 08.06.16.
 * @since 0.60.0
 */
@Component
public class MetricsFilter implements Filter {

    private final MetricRegistry metricRegistry;

    @Autowired
    public MetricsFilter(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String method = ((HttpServletRequest) request).getMethod().toLowerCase();
        final Timer.Context context = metricRegistry.timer("timer.http." + method).time();
        try {
            chain.doFilter(request, response);
        } finally {
            if (response != null) {
                final int status = ((HttpServletResponse) response).getStatus();
                metricRegistry.counter("counter.http." + method + "." + status).inc();
                context.stop();
            }
        }
    }

    @Override
    public void destroy() {
    }

}
