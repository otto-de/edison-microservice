package de.otto.edison.status.controller;

import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static de.otto.edison.status.controller.StatusRepresentation.statusRepresentationOf;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class StatusController {

    @Autowired
    private ApplicationStatusAggregator aggregator;

    @RequestMapping(
            value = "/internal/status",
            produces = {
                    "application/hal+json",
                    "application/vnd.otto.monitoring.status+json",
                    "application/json"},
            method = GET
    )
    public StatusRepresentation getStatusAsJson() {
        return statusRepresentationOf(aggregator.aggregatedStatus());
    }

    @RequestMapping(
            value = "/internal/status",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml() {
        return new ModelAndView("status", "status", aggregator.aggregatedStatus());
    }

}

