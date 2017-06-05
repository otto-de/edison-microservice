package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import de.otto.edison.authentication.configuration.LdapProperties;

import java.security.GeneralSecurityException;

/**
 * A Factory used to create LDAPConnections usering LdapProperties.
 */
public class LdapConnectionFactory {

    private static final SSLUtil SSL_UTIL = new SSLUtil();
    private final LdapProperties ldapProperties;

    public LdapConnectionFactory(final LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    public LDAPConnection buildLdapConnection() throws GeneralSecurityException, LDAPException {
        final LDAPConnection ldapConnection = new LDAPConnection(ldapProperties.getHost(), ldapProperties.getPort());
        ldapConnection.processExtendedOperation(new StartTLSExtendedRequest(SSL_UTIL.createSSLContext()));
        return ldapConnection;
    }

}
