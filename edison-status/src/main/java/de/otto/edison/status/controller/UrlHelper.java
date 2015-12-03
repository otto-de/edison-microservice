package de.otto.edison.status.controller;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

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

    public static URL url(final String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
