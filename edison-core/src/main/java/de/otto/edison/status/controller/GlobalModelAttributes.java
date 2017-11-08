package de.otto.edison.status.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    WebEndpointProperties webEndpointProperties;

    @Autowired
    public GlobalModelAttributes(WebEndpointProperties  webEndpointProperties) {
        this.webEndpointProperties = webEndpointProperties;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("managementContextPath", webEndpointProperties.getBasePath());
    }

}
