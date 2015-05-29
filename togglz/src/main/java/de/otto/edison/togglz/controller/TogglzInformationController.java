package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.FeatureClassProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.otto.edison.togglz.controller.TogglzRepresentation.togglzRepresentation;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class TogglzInformationController {

    private final FeatureClassProvider featureClassProvider;

    @Autowired
    public TogglzInformationController(final FeatureClassProvider featureClassProvider) {
        this.featureClassProvider = featureClassProvider;
    }

    @RequestMapping(
            value = "/internal/status/togglz",
            produces = {"application/vnd.otto.monitoring.status+json", "application/json"},
            method = GET
    )
    public TogglzRepresentation getStatusAsJson() {
        return togglzRepresentation(featureClassProvider);
    }
}
