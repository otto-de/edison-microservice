package de.otto.edison.registry.client;

/**
 * A client to access a discovery service.
 *
 * @author Guido Steinacker
 * @since 16.09.15
 */
public interface RegistryClient {

    /**
     * Registers the service at a configured ServiceDiscovery.
     */
    void registerService();

    boolean isRunning();
}
