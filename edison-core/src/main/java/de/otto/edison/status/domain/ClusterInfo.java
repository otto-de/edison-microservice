package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;

import java.util.function.Supplier;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Beta
public class ClusterInfo {

    private final Supplier<String> color;
    private final Supplier<String> colorState;

    public ClusterInfo(final String color, final String colorState) {
        this.color = () -> color;
        this.colorState = () -> colorState;
    }

    public ClusterInfo(final Supplier<String> colorSupplier, final Supplier<String> colorStateSupplier) {
        color = colorSupplier;
        colorState = colorStateSupplier;
    }

    public String getColor() {
        return color.get();
    }

    public String getColorState() {
        return colorState.get();
    }
}
