package de.otto.edison.status.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties({ServerProperties.class, EdisonApplicationProperties.class})
public class InternalController {

    private final EdisonApplicationProperties properties;
    private final ServerProperties serverProperties;

    public InternalController(final EdisonApplicationProperties properties,
                              final ServerProperties serverProperties) {
        this.properties = properties;
        this.serverProperties = serverProperties;
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}")
    public void redirectToStatus(final HttpServletResponse response) throws IOException {
        response.sendRedirect(String.format("%s%s/status",
                serverProperties.getServlet().getContextPath(),
                properties.getManagement().getBasePath())
        );
    }
}
