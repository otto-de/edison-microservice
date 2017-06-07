package de.otto.edison.metrics.load;

import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Beta
@RestController
@ConditionalOnProperty(prefix = "edison.metrics.load", name = "enabled", havingValue = "true")
public class MetricsLoadController {

    private LoadDetector loadDetector;

    @Autowired
    public MetricsLoadController(final LoadDetector loadDetector) {
        this.loadDetector = loadDetector;
    }

    @RequestMapping(
            value = "${management.context-path}/load",
            produces = {"application/json"},
            method = GET
    )
    public MetricsLoadRepresentation getStatusAsJson() {
        return new MetricsLoadRepresentation(loadDetector.getStatus());
    }

}

