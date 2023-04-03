package de.otto.edison.logging;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Order(Ordered.LOWEST_PRECEDENCE)
public class LogHeadersToMDCFilter extends OncePerRequestFilter {

    private final List<String> headerNames;

    public LogHeadersToMDCFilter(List<String> headerNames) {
        this.headerNames = headerNames;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            addHeaders(headerNames, request);
            filterChain.doFilter(request, response);
        } finally {
            removeHeaders(headerNames);
        }

    }

    private void removeHeaders(List<String> headerNames) {
        headerNames.forEach(MDC::remove);
    }

    private void addHeaders(List<String> headerNames, HttpServletRequest request) {
        headerNames.forEach(headerName -> {
            String value = request.getHeader(headerName);
            if (value != null) {
                MDC.put(headerName, value);
            }
        });
    }
}
