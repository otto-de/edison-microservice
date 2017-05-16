package de.otto.edison.status.controller;

import de.otto.edison.status.controller.StatusRepresentation.DependencyRepresentation;
import de.otto.edison.status.domain.Criticality;
import de.otto.edison.status.domain.ExternalDependency;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

import static de.otto.edison.status.controller.StatusRepresentation.statusRepresentationOf;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class StatusController {

    @Autowired
    private ApplicationStatusAggregator aggregator;
    @Autowired
    private ExternalDependencies externalDependencies;
    @Autowired(required = false)
    private Criticality criticality;

    @RequestMapping(
            value = "/internal/status",
            produces = {
                    "application/hal+json",
                    "application/vnd.otto.monitoring.status+json",
                    "application/json"},
            method = GET
    )
    public StatusRepresentation getStatusAsJson() {
        return statusRepresentationOf(
                aggregator.aggregatedStatus(),
                criticality,
                externalDependencies.getDependencies());
    }

    @RequestMapping(
            value = "/internal/status",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml() {
        return new ModelAndView("status", new HashMap<String,Object>() {{
            put("status", aggregator.aggregatedStatus());
            put("criticality", criticality);
            put("dependencies", externalDependencies.getDependencies()
                    .stream()
                    .sorted(comparing(ExternalDependency::getType).thenComparing(ExternalDependency::getName))
                    .map(DependencyRepresentation::new)
                    .collect(toList()));
        }});
    }

}

