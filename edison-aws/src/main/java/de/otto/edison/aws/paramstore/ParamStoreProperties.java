package de.otto.edison.aws.paramstore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.config.paramstore")
public class ParamStoreProperties {
    private boolean enabled;
    private String path;
    private boolean addWithLowestPrecedence;

    public ParamStoreProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public boolean isAddWithLowestPrecedence() {
        return addWithLowestPrecedence;
    }

    public void setAddWithLowestPrecedence(final boolean addWithLowestPrecedence) {
        this.addWithLowestPrecedence = addWithLowestPrecedence;
    }
}
