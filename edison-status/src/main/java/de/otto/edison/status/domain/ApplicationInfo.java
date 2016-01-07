package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import static java.util.Objects.requireNonNull;

/**
 * Information about the application, like name, description, group and environment.
 */
@Immutable
public class ApplicationInfo {

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
        this.name = requireNonNull(name);
        this.description = description;
        this.group = group;
        this.environment = environment;
    }

    public static ApplicationInfo applicationInfo(final String name,
                                                  final String description,
                                                  final String group,
                                                  final String environment) {
        return new ApplicationInfo(name, description, group, environment);
    }
}
