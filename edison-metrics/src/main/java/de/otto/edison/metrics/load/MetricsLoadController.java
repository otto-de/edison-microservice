package de.otto.edison.metrics.load;

import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Beta
@RestController
public class MetricsLoadController {

    @Autowired
    private LoadDetector loadDetector;

    public MetricsLoadController() {
    }

    public MetricsLoadController(final LoadDetector loadDetector) {
        this.loadDetector = loadDetector;
    }

    @RequestMapping(
            value = "/internal/load",
            produces = {"application/vnd.otto.monitoring.metrics+json", "application/json"},
            method = GET
    )
    public MetricsLoadRepresentation getStatusAsJson() {
        return new MetricsLoadRepresentation(loadDetector.getStatus());
    }

}

