package de.otto.edison.registry.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.serviceregistry")
public class ServiceRegistryProperties {
    private String servers;
    /**
     * URL of the registered service
     */
    private String service;
    /**
     * Expire the registration after N minutes
     */
    private long expireAfter = 15L;
    /**
     * Refresh registration after N minutes
     */
    private long refreshAfter = 5L;

    public String getServers() {
        return servers;
    }

    public ServiceRegistryProperties setServers(String servers) {
        this.servers = servers;
        return this;
    }

    public String getService() {
        return service;
    }

    public ServiceRegistryProperties setService(String service) {
        this.service = service;
        return this;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public ServiceRegistryProperties setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
        return this;
    }

    public long getRefreshAfter() {
        return refreshAfter;
    }

    public ServiceRegistryProperties setRefreshAfter(long refreshAfter) {
        this.refreshAfter = refreshAfter;
        return this;
    }
}
