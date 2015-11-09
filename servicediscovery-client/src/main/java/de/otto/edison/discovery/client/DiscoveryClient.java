package de.otto.edison.discovery.client;

/**
 * A client to access a discovery service.
 *
 * @author Guido Steinacker
 * @since 16.09.15
 */
public interface DiscoveryClient {

    /**
     * Registers the service at a configured ServiceDiscovery.
     */
    void registerService();

}
