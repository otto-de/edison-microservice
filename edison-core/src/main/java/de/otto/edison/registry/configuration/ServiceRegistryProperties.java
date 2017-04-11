package de.otto.edison.registry.configuration;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@ConfigurationProperties(prefix = "edison.serviceregistry")
@Validated
public class ServiceRegistryProperties {

    /**
     * serviceregistry client enabled or disabled
     */
    private boolean enabled = true;

    /**
     * URL of jobtrigger
     */
    private String servers;

    /**
     * URL of the registered service
     */
    @URL
    private String service;
    /**
     * Expire the registration after N minutes
     */
    @Min(1)
    private long expireAfter = 15L;
    /**
     * Refresh registration after N minutes
     */
    @Min(1)
    private long refreshAfter = 5L;

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    public long getRefreshAfter() {
        return refreshAfter;
    }

    public void setRefreshAfter(long refreshAfter) {
        this.refreshAfter = refreshAfter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServiceRegistryProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public String toString() {
        return "ServiceRegistryProperties{" +
                "enabled=" + enabled +
                ", servers='" + servers + '\'' +
                ", service='" + service + '\'' +
                ", expireAfter=" + expireAfter +
                ", refreshAfter=" + refreshAfter +
                '}';
    }
}
