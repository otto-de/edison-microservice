package de.otto.edison.status.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    ManagementServerProperties managementServerProperties;

    @Autowired
    public GlobalModelAttributes(ManagementServerProperties managementServerProperties) {
        this.managementServerProperties = managementServerProperties;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("managementContextPath", managementServerProperties.getContextPath());
    }

}
