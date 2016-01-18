package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

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
        if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationInfo that = (ApplicationInfo) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        return !(environment != null ? !environment.equals(that.environment) : that.environment != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationInfo{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", group='" + group + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
