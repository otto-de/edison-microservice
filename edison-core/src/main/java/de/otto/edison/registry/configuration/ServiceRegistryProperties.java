package de.otto.edison.registry.configuration;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "edison.serviceregistry")
public class ServiceRegistryProperties {

    @NotEmpty
    private String servers;
    /**
     * URL of the registered service
     */
    @NotNull @URL
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
}
