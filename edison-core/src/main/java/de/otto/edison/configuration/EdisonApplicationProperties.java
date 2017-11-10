package de.otto.edison.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties used to configure the status page.
 */
@ConfigurationProperties(prefix = "edison.application")
public class EdisonApplicationProperties {

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

    private Management management = new Management("/internal");

    /**
     * Only used in tests.
     *
     * @param title Human-readable title of the application
     * @param group Application group
     * @param environment Environment / stage of the application
     * @param description Human-readable description of the application
     * @return StatusProperties
     */
    public static EdisonApplicationProperties edisonApplicationProperties(final String title,
                                                                          final String group,
                                                                          final String environment,
                                                                          final String description) {
        final EdisonApplicationProperties edisonApplicationProperties = new EdisonApplicationProperties();
        edisonApplicationProperties.setTitle(title);
        edisonApplicationProperties.setGroup(group);
        edisonApplicationProperties.setEnvironment(environment);
        edisonApplicationProperties.setDescription(description);
        edisonApplicationProperties.setManagement(new Management("/internal"));
        return edisonApplicationProperties;
    }

    /**
     * Only used in tests.
     *
     * @param title Human-readable title of the application
     * @param group Application group
     * @param environment Environment / stage of the application
     * @param description Human-readable description of the application
     * @param management management properties, i.e. path for /internal stuff
     * @return StatusProperties
     */
    public static EdisonApplicationProperties edisonApplicationProperties(final String title,
                                                                          final String group,
                                                                          final String environment,
                                                                          final String description,
                                                                          final Management management) {
        final EdisonApplicationProperties edisonApplicationProperties = new EdisonApplicationProperties();
        edisonApplicationProperties.setTitle(title);
        edisonApplicationProperties.setGroup(group);
        edisonApplicationProperties.setEnvironment(environment);
        edisonApplicationProperties.setDescription(description);
        edisonApplicationProperties.setManagement(management);
        return edisonApplicationProperties;
    }

    public String getTitle() {
        return title;
    }

    public EdisonApplicationProperties setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public EdisonApplicationProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public EdisonApplicationProperties setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public EdisonApplicationProperties setDescription(String description) {
        this.description = description;
        return this;
    }

    public Management getManagement() {
        return management;
    }

    public void setManagement(Management management) {
        this.management = management;
    }

    public static class Management {
        private String basePath = "/internal";

        public Management(final String basePath) {
            this.basePath = basePath;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
}
