package de.otto.edison.jobs.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Guido Steinacker
 * @since 21.08.15
 */
public class UrlHelper {

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
}
