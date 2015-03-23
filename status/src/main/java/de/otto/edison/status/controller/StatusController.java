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
        return new ModelAndView("status") {{
                addObject("status", applicationStatus.getStatus().name());
                addObject("name", applicationStatus.getName());
                addObject("version", applicationStatus.getVersionInfo().getVersion());
                addObject("commit", applicationStatus.getVersionInfo().getCommit());
                addObject("statusDetails", statusDetails(applicationStatus));
                addObject("hostname", applicationStatus.getHostName());
        }};
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

