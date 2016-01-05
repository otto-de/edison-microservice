package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.List;

@Immutable
public class ApplicationInfo {

    private final String name;
    private final String description;
    private final String group;
    private final String environment;


    private ApplicationInfo(final String name,
                            final String description,
                            final String group,
                            final String environment) {
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public String getEnvironment() {
        return environment;
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
