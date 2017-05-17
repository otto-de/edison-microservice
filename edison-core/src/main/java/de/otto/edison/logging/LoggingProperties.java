package de.otto.edison.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties to configure reporting of edison-core metrics.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.logging")
@Validated
public class LoggingProperties {

    private Header header;
    private Ui ui;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Ui getUi() {
        return ui;
    }

    public void setUi(Ui ui) {
        this.ui = ui;
    }

    public static class Header {
        private boolean enabled = true;
        private String names = "X-Origin";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getNames() {
            return names;
        }

        public void setNames(String names) {
            this.names = names;
        }
    }
    public static class Ui  {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
