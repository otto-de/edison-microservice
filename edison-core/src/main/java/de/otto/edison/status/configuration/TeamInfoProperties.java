package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties used to configure the team information on the status page.
 */
@ConfigurationProperties(prefix = "edison.status.team")
public class TeamInfoProperties {
    /** The name of the team. */
    private String name;
    /** A technical contact like, for example, a phone number or mail address. */
    private String technicalContact;
    /** A business contact like, for example, a phone number or mail address. */
    private String businessContact;

    public static TeamInfoProperties teamInfoProperties(final String name, final String technicalContact, final String businessContact) {
        final TeamInfoProperties teamInfoProperties = new TeamInfoProperties();
        teamInfoProperties.name = name;
        teamInfoProperties.technicalContact = technicalContact;
        teamInfoProperties.businessContact = businessContact;
        return teamInfoProperties;
    }

    public String getName() {
        return name;
    }

    public TeamInfoProperties setName(String name) {
        this.name = name;
        return this;
    }

    public String getTechnicalContact() {
        return technicalContact;
    }

    public TeamInfoProperties setTechnicalContact(String technicalContact) {
        this.technicalContact = technicalContact;
        return this;
    }

    public String getBusinessContact() {
        return businessContact;
    }

    public TeamInfoProperties setBusinessContact(String businessContact) {
        this.businessContact = businessContact;
        return this;
    }
}
