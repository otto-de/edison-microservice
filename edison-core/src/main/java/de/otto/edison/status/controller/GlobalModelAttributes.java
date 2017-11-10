package de.otto.edison.status.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final WebEndpointProperties webEndpointProperties;
    private final EdisonApplicationProperties edisonApplicationProperties;

    @Autowired
    public GlobalModelAttributes(final WebEndpointProperties  webEndpointProperties,
                                 final EdisonApplicationProperties edisonApplicationProperties) {
        this.webEndpointProperties = webEndpointProperties;
        this.edisonApplicationProperties = edisonApplicationProperties;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("webEndpointBasePath", webEndpointProperties.getBasePath());
        model.addAttribute("edisonManagementBasePath", edisonApplicationProperties.getManagement().getBasePath());
    }

}
