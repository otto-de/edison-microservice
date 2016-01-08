package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.StringJoiner;

/**
 * Information about the application, like name, description, group and environment.
 */
@Immutable
public class ApplicationInfo {

    /**
     * Identifier of the service.
     * The appId is constructed from environment, group and name like this: '/live/shop/shoppingcart'. With
     * environment = "live", group = "" and name = "shoppingcart", the appId would only contain the specified
     * parts: /live/shoppingcart.
     *
     * The identifier is used to identify a service in a group, deployed in some staging environment. You
     * can find this information in the application section of the /internal/about page. If you are specifying
     * dependencies to other services using {@link de.otto.edison.about.spec.ServiceSpec}s, the ServicesSpec#appId
     * should match the appId of the services you are depending on.
     */
    public final String appId;
    /** The name of the application. */
    public final String name;
    /** A short description of the application's purpose. */
    public final String description;
    /** The group of services, this application is part of. Examples are 'order', 'search' or 'navigation'. */
    public final String group;
    /** The staging environment, this application is running in. Examples are 'develop', 'prelive' or 'live'. */
    public final String environment;


    private ApplicationInfo(final String name,
                            final String description,
                            final String group,
                            final String environment) {
        if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
        this.name = name;
        this.description = description;
        this.group = group;
        this.environment = environment;
        this.appId = buildAppId(environment, group, name);
    }

    public static ApplicationInfo applicationInfo(final String name,
                                                  final String description,
                                                  final String group,
                                                  final String environment) {
        return new ApplicationInfo(name, description, group, environment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationInfo that = (ApplicationInfo) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        return !(environment != null ? !environment.equals(that.environment) : that.environment != null);

    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        return result;
    }

    private static String buildAppId(final String environment, final String group, final String name) {
        final StringJoiner joiner = new StringJoiner("/", "/", "");
        if (!environment.isEmpty()) joiner.add(environment);
        if (!group.isEmpty()) joiner.add(group);
        return joiner.add(name).toString();
    }
}
