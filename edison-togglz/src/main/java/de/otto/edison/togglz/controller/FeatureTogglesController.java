package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.FeatureClassProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class FeatureTogglesController {

    private final FeatureClassProvider featureClassProvider;

    @Autowired
    public FeatureTogglesController(final FeatureClassProvider featureClassProvider) {
        this.featureClassProvider = featureClassProvider;
    }

    @RequestMapping(
            value = "/internal/toggles",
            produces = {
                    "application/vnd.otto.monitoring.status+json",
                    "application/json"},
            method = GET
    )
    public FeatureTogglesRepresentation getStatusAsJson() {
        return togglzRepresentation(featureClassProvider);
    }
}
