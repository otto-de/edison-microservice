package de.otto.edison.status.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
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
@EnableConfigurationProperties({ServerProperties.class, ManagementServerProperties.class})
public class InternalController {

    private final ManagementServerProperties managementServerProperties;
    private final ServerProperties serverProperties;

    public InternalController(final ManagementServerProperties managementServerProperties,
                              final ServerProperties serverProperties) {
        this.managementServerProperties = managementServerProperties;
        this.serverProperties = serverProperties;
    }

    @RequestMapping(value = "${management.context-path}")
    public void redirectToStatus(final HttpServletResponse response) throws IOException {
        response.sendRedirect(String.format("%s%s/status", serverProperties.getContextPath(), managementServerProperties.getContextPath()));
    }
}
