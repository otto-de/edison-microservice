package de.otto.edison.togglz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;

import static de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class FeatureTogglesController {

    private final FeatureManager featureManager;

    @Autowired
    public FeatureTogglesController(final FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/toggles",
            produces = {
                    "application/vnd.otto.monitoring.status+json",
                    "application/json"},
            method = GET
    )
    public FeatureTogglesRepresentation getStatusAsJson() {
        return togglzRepresentation(featureManager);
    }
}
