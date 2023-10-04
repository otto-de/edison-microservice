package de.otto.edison.acceptance.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.otto.edison.acceptance.SpringTestBaseWithComponentScan;
import de.otto.edison.registry.client.AsyncHttpRegistryClient;
import de.otto.edison.registry.configuration.ServiceRegistrySecurityOAuthProperties;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SpringTestBaseWithComponentScan.class})
@ActiveProfiles("test")
public class OAuth2TokenProviderAcceptanceTest {
    @Autowired
    private ServiceRegistrySecurityOAuthProperties serviceRegistrySecurityOAuthProperties;

    @Autowired
    private AsyncHttpRegistryClient asyncHttpRegistryClient;

    @Test
    public void shouldInstantiateRegistryClientWithoutExistingProperties() {
        // then
        assertNotNull(asyncHttpRegistryClient);
        assertFalse(serviceRegistrySecurityOAuthProperties.enabled());
        assertEquals(serviceRegistrySecurityOAuthProperties.timeoutSeconds(), 10);
    }
}
