package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties used to configure the status page.
 */
@ConfigurationProperties(prefix = "edison.status.application")
public class StatusProperties {

    /**
     * A short title that is used in the top navigation and the html title tag.
     */
    private String title = "Edison µService";
    /**
     * Information about the group of services this service is belonging to.
     */
    private String group = "";
    /**
     * The staging environment (like develop, prelive, live) of the service.
     */
    private String environment = "unknown";
    /**
     * A human-readable short description of the application's purpose.
     */
    private String description = "";

    public static StatusProperties statusProperties(final String title,
                                                    final String group,
                                                    final String environment,
                                                    final String description) {
        StatusProperties statusProperties = new StatusProperties();
        statusProperties.setTitle(title);
        statusProperties.setGroup(group);
        statusProperties.setEnvironment(environment);
        statusProperties.setDescription(description);
        return statusProperties;
    }

    public String getTitle() {
        return title;
    }

    public StatusProperties setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public StatusProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public StatusProperties setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public StatusProperties setDescription(String description) {
        this.description = description;
        return this;
    }
}
