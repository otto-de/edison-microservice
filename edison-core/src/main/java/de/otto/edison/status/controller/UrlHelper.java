package de.otto.edison.status.controller;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentServletMapping;

/**
 * @author Guido Steinacker
 * @since 21.08.15
 */
class UrlHelper {

    private UrlHelper() {}

    /**
     * Returns the baseUri of the request: http://www.example.com:8080/example
     *
     * @param request the current HttpServletRequest
     * @return base uri including protocol, host, port and context path
     */
    public static String baseUriOf(final HttpServletRequest request) {
        final StringBuffer requestUrl = request.getRequestURL();
        return requestUrl != null
                ? requestUrl.substring(0, requestUrl.indexOf(request.getServletPath()))
                : "";
    }

    /**
     * Returns an absolute URL for the specified path.
     *
     * Example: If the current request URL is http://example.org/helloworld/internal/status,
     * {@code absoluteHrefOf("/internal/health")} will return http://example.org/helloworld/internal/health (with
     * helloworld as servletContextPath).
     *
     * This method relies on Spring's {@link org.springframework.web.context.request.RequestContextHolder} to find
     * the current request.
     *
     * @param path
     * @return
     */
    public static String absoluteHrefOf(final String path) {
        try {
            return fromCurrentServletMapping().path(path).build().toString();
        } catch (final IllegalStateException e) {
            return path;
        }
    }

    public static URL url(final String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
