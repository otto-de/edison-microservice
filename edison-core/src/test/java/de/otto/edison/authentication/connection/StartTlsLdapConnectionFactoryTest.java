package de.otto.edison.authentication.connection;

import com.unboundid.ldap.sdk.LDAPException;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StartTlsLdapConnectionFactoryTest {

    @Test
    public void shouldTryToBuildLdapConnection() {
        final LdapProperties properties = mock(LdapProperties.class);
        when(properties.getHost()).thenReturn("foo");
        when(properties.getPort()).thenReturn(42);
        assertThrows(LDAPException.class, () -> {
            new StartTlsLdapConnectionFactory(properties).buildLdapConnection();
        });
    }
}