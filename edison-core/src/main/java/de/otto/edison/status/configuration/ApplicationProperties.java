package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties used to configure the status page.
 */
@ConfigurationProperties(prefix = "edison.status.application")
public class ApplicationProperties {

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

    /**
     * Only used in tests.
     *
     * @param title Human-readable title of the application
     * @param group Application group
     * @param environment Environment / stage of the application
     * @param description Human-readable description of the application
     * @return StatusProperties
     */
    public static ApplicationProperties statusProperties(final String title,
                                                         final String group,
                                                         final String environment,
                                                         final String description) {
        ApplicationProperties applicationProperties = new ApplicationProperties();
        applicationProperties.setTitle(title);
        applicationProperties.setGroup(group);
        applicationProperties.setEnvironment(environment);
        applicationProperties.setDescription(description);
        return applicationProperties;
    }

    public String getTitle() {
        return title;
    }

    public ApplicationProperties setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ApplicationProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public ApplicationProperties setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationProperties setDescription(String description) {
        this.description = description;
        return this;
    }

}
