package de.otto.edison.status.domain;

import de.otto.edison.configuration.EdisonApplicationProperties;
import net.jcip.annotations.Immutable;

import java.util.Objects;

/**
 * Information about the application, like name, description, group and environment.
 */
@Immutable
public class ApplicationInfo {

    /**
     * The name of the application.
     */
    public final String name;
    /**
     * Human readable title of the application
     */
    public final String title;
    /**
     * A short description of the application's purpose.
     */
    public final String description;
    /**
     * The group of services, this application is part of. Examples are 'order', 'search' or 'navigation'.
     */
    public final String group;
    /**
     * The staging environment, this application is running in. Examples are 'develop', 'prelive' or 'live'.
     */
    public final String environment;


    private ApplicationInfo(final String name,
                            final EdisonApplicationProperties applicationInfoProperties) {
        if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
        this.name = name;
        this.title = applicationInfoProperties.getTitle();
        this.description = applicationInfoProperties.getDescription();
        this.group = applicationInfoProperties.getGroup();
        this.environment = applicationInfoProperties.getEnvironment();
    }

    public static ApplicationInfo applicationInfo(final String serviceName, final EdisonApplicationProperties statusProps) {
        return new ApplicationInfo(serviceName, statusProps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationInfo that = (ApplicationInfo) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(group, that.group) &&
                Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, group, environment);
    }

    @Override
    public String toString() {
        return "ApplicationInfo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", group='" + group + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
