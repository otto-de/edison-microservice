package de.otto.edison.status.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirect requests to /internal to /internal/status.
 *
 * This can be disabled by setting edison.status.redirect-internal.enabled=false in your application.properties.
 *
 * Created by guido on 05.02.16.
 */
@Controller
@ConditionalOnProperty(name = "edison.status.redirect-internal.enabled", havingValue = "true", matchIfMissing = true)
public class InternalController {

    @Value("${server.context-path}")
    private String serverContextPath;

    @RequestMapping(value = "${management.context-path}")
    public void redirectToStatus(final HttpServletResponse response) throws IOException {
        response.sendRedirect(String.format("%s/internal/status", serverContextPath));
    }
}
