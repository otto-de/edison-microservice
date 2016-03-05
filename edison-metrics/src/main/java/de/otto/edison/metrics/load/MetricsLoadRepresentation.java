package de.otto.edison.metrics.load;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.edison.annotations.Beta;
import de.otto.edison.metrics.load.LoadDetector.Status;
import jdk.nashorn.internal.ir.annotations.Immutable;

@Beta
@Immutable
public class MetricsLoadRepresentation {

    public final Status status;

    @JsonCreator
    public MetricsLoadRepresentation(@JsonProperty("status") Status status) {
        this.status = status;
    }

}
