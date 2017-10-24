package de.otto.edison.authentication.connection;

import com.unboundid.ldap.sdk.LDAPException;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SSLLdapConnectionFactoryTest {

    @Test(expected = LDAPException.class)
    public void shouldTryToBuildLdapConnection() throws GeneralSecurityException, LDAPException {
        LdapProperties properties = mock(LdapProperties.class);
        when(properties.getHost()).thenReturn("foo");
        when(properties.getPort()).thenReturn(42);
        new SSLLdapConnectionFactory(properties).buildLdapConnection();
    }
}
