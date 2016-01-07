package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.otto.edison.status.controller.ApplicationStatusRepresentation.statusRepresentationOf;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@ConfigurationProperties(value = "endpoints.status", ignoreUnknownFields = false)
@RestController
public class StatusController {

    private static final String SYSTEM_START_TIME = now().format(ofLocalizedDateTime(LONG));

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
        return statusRepresentationOf(aggregator.aggregatedStatus());
    }

    @RequestMapping(
            value = "/internal/status",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml() {
        final ApplicationStatus applicationStatus = aggregator.aggregatedStatus();
        return new ModelAndView("status", "status", aggregator.aggregatedStatus());
    }

    private Collection<Map<String, ?>> statusDetails(final List<StatusDetail> statusDetails) {
        return statusDetails.stream()
                .map(detail ->
                            new LinkedHashMap<String, Object>() {{
                                put("key", detail.getName());
                                put("status", detail.getStatus().name());
                                put("message", detail.getMessage());
                                put("additionalDetails", detail.getDetails());
                            }})
                .collect(Collectors.toList());
    }

}

