package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.jcip.annotations.Immutable;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterInfo {

    public final String staging;
    public final String color;

    private ClusterInfo(final String staging, final String color) {
        this.staging = staging;
        this.color = color;
    }

    public static ClusterInfo clusterInfo(final String staging, final String color) {
        return new ClusterInfo(staging, color);
    }

    public String getStaging() {
        return staging;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClusterInfo that = (ClusterInfo) o;

        if (staging != that.staging) {
            return false;
        }
        return color == that.color;
    }

    @Override
    public int hashCode() {
        int result = staging != null ? staging.hashCode() : 0;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClusterInfo{" +
                "staging=" + staging +
                ", color=" + color +
                '}';
    }
}
