package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.otto.edison.status.controller.ApplicationStatusRepresentation.statusRepresentationOf;
import static de.otto.edison.status.controller.UrlHelper.baseUriOf;
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
    public ModelAndView getStatusAsHtml(final HttpServletRequest request) {
        final ApplicationStatus applicationStatus = aggregator.aggregatedStatus();
        return new ModelAndView("status") {{
            addObject("status", applicationStatus.getStatus().name());
            addObject("name", applicationStatus.getName());
            addObject("hostname", applicationStatus.getHostName());
            addObject("systemtime", now().format(ofLocalizedDateTime(LONG)));
            addObject("systemstarttime", SYSTEM_START_TIME);
            addObject("version", applicationStatus.getVersionInfo().getVersion());
            addObject("commit", applicationStatus.getVersionInfo().getCommit());
            addObject("statusDetails", statusDetails(applicationStatus));
        }};
    }

    private Collection<Map<String, ?>> statusDetails(ApplicationStatus applicationStatus) {
        return applicationStatus.getStatusDetails().stream()
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

