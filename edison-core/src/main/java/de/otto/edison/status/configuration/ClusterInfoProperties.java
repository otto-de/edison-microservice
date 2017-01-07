package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.lang.String.format;

/**
 * Properties used to configure the display of additional status information on /internal/status and /internal/info.
 */
@ConfigurationProperties(prefix = "edison.status.cluster")
public class ClusterInfoProperties {

    /**
     * Enable/Disable the display of additional status information on /internal/status and /internal/info
     */
    private boolean enabled = false;

    /**
     * HTTP-Header used to identify the current color of the application cluster.
     */
    private String colorHeader = "X-Color";
    /**
     * Http-Header used to identify the current stage of the application cluster.
     */
    private String colorStateHeader = "X-Staging";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getColorHeader() {
        return colorHeader;
    }

    public void setColorHeader(String colorHeader) {
        this.colorHeader = colorHeader;
    }

    public String getColorStateHeader() {
        return colorStateHeader;
    }

    public void setColorStateHeader(String colorStateHeader) {
        this.colorStateHeader = colorStateHeader;
    }

}
