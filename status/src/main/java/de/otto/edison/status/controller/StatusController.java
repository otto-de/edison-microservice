package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.otto.edison.status.controller.ApplicationStatusRepresentation.statusRepresentationOf;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@ConfigurationProperties(value = "endpoints.status", ignoreUnknownFields = false)
@RestController
public class StatusController {

    @Autowired
    private ApplicationStatusAggregator aggregator;

    public StatusController() {
    }

    public StatusController(final ApplicationStatusAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @RequestMapping(
            value = "/internal/status",
            produces = {"application/vnd.otto.monitoring.status+json", "application/json"},
            method = GET
    )
    public ApplicationStatusRepresentation getStatusAsJson() {
        return statusRepresentationOf(aggregator.aggregate());
    }

    @RequestMapping(
            value = "/internal/status",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml() {
        final ApplicationStatus applicationStatus = aggregator.aggregate();
        final ModelAndView modelAndView = new ModelAndView("status");
        modelAndView.addObject("status", applicationStatus.getStatus().name());
        modelAndView.addObject("name", applicationStatus.getName());
        modelAndView.addObject("statusDetails", statusDetails(applicationStatus));
        return modelAndView;
    }

    private Collection<Map<String, String>> statusDetails(ApplicationStatus applicationStatus) {
        return applicationStatus.getStatusDetails().stream()
                .map(detail ->
                            new LinkedHashMap<String, String>() {{
                                put("key", detail.getName());
                                put("status", detail.getStatus().name());
                                put("message", detail.getMessage());
                                putAll(detail.getDetails());
                            }})
                .collect(Collectors.toList());
    }

}

