package de.otto.edison.togglz.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * Configuration properties used to configure edison-togglz
 */
@ConfigurationProperties(prefix = "edison.togglz")
public class TogglzProperties {

    /**
     * Number of millis used to cache toggle state. cache-ttl=0 will disable caching.
     */
    @Min(0)
    private int cacheTtl = 5000;
    /**
     * Enable / Disable the Togglz web console.
     */
    @Valid
    private Console console = new Console();

    public int getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(int cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public static class Console {
        /**
         * Enable / disable the Togglz web console.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
