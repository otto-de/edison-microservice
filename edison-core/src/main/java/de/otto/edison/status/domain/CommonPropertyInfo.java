package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonPropertyInfo {
    private String key = "";
    private String value = "";
    private String description = "";

    public CommonPropertyInfo() {
    }

    public CommonPropertyInfo(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonPropertyInfo)) return false;

        CommonPropertyInfo that = (CommonPropertyInfo) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonPropertyInfo{");
        sb.append(", key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
