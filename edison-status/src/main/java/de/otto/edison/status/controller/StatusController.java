package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ClusterInfo;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

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
    public StatusRepresentation getStatusAsJson(final HttpServletRequest request) {
        return statusRepresentationOf(aggregator.aggregatedStatus(), clusterInfoOf(request));
    }

    @RequestMapping(
            value = "/internal/status",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml(final HttpServletRequest request) {
        return new ModelAndView("status", "status", aggregator.aggregatedStatus());
    }

    private ClusterInfo clusterInfoOf(final HttpServletRequest request) {
        final String staging = request.getHeader("x-staging");
        final String color = request.getHeader("x-color");

        return ClusterInfo.clusterInfo(StringUtils.isEmpty(staging) ? "undefined" : staging,
                StringUtils.isEmpty(color) ? "UNDEFINED" : color);
    }
}

