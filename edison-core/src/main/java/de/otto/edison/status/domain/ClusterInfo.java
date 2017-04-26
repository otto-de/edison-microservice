package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;

import java.util.function.Supplier;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Information about the service cluster.
 */
@JsonInclude(NON_EMPTY)
@Beta
public class ClusterInfo {

    private final Supplier<String> color;
    private final Supplier<String> colorState;

    /**
     * Creates a ClusterInfo instance with color and colorState information.
     *
     * @param color the color of the cluster in Blue/Green deployment scenarios.
     * @param colorState the state of the color, for example "staged" or "live" to determine if the color is currently
     *                   in production, or not.
     */
    public ClusterInfo(final String color, final String colorState) {
        this.color = () -> color;
        this.colorState = () -> colorState;
    }

    /**
     * Creates a ClusterInfo instance with {@link Supplier suppliers} for color and colorState information.
     *
     * @param colorSupplier the Supplier for the color of the cluster in Blue/Green deployment scenarios.
     * @param colorStateSupplier the Supplier for the state of the cluster, for example "staged" or "live",
     *                           to determine if the color is currently in production, or not.
     */
    public ClusterInfo(final Supplier<String> colorSupplier, final Supplier<String> colorStateSupplier) {
        color = colorSupplier;
        colorState = colorStateSupplier;
    }

    /**
     * Creates a ClusterInfo instance with color and colorState information.
     *
     * @param color the color of the cluster in Blue/Green deployment scenarios.
     * @param colorState the state of the color, for example "staged" or "live" to determine if the color is currently
     *                   in production, or not.
     */
    public static ClusterInfo clusterInfo(final String color, final String colorState) {
        return new ClusterInfo(() -> color, () -> colorState);
    }

    /**
     * Creates a ClusterInfo instance with {@link Supplier suppliers} for color and colorState information.
     *
     * @param colorSupplier the Supplier for the color of the cluster in Blue/Green deployment scenarios.
     * @param colorStateSupplier the Supplier for the state of the cluster, for example "staged" or "live",
     *                           to determine if the color is currently in production, or not.
     */
    public static ClusterInfo clusterInfo(final Supplier<String> colorSupplier, final Supplier<String> colorStateSupplier) {
        return new ClusterInfo(colorSupplier, colorStateSupplier);
    }

    /**
     *
     * @return the color of the cluster in Blue/Green deployment scenarios.
     */
    public String getColor() {
        return color.get();
    }

    /**
     *
     * @return the state of the color, for example "staged" or "live" to determine if the color is currently
     * in production, or not.
     */
    public String getColorState() {
        return colorState.get();
    }

    /**
     * Blue/Green deployments enabled, or not.
     *
     * @return boolean
     */
    @JsonIgnore
    public boolean isEnabled() {
        return !getColor().isEmpty() || !getColorState().isEmpty();
    }
}
