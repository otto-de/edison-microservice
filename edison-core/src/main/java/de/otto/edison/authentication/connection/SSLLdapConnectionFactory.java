package de.otto.edison.authentication.connection;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import de.otto.edison.authentication.configuration.LdapProperties;

import javax.net.ssl.SSLSocketFactory;
import java.security.GeneralSecurityException;

public class SSLLdapConnectionFactory implements LdapConnectionFactory {

    private static final SSLUtil SSL_UTIL = new SSLUtil();
    private final LdapProperties ldapProperties;

    public SSLLdapConnectionFactory(final LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    public LDAPConnection buildLdapConnection() throws GeneralSecurityException, LDAPException {
        SSLSocketFactory sslSocketFactory = SSL_UTIL.createSSLSocketFactory();
        return new LDAPConnection(sslSocketFactory, ldapProperties.getHost(), ldapProperties.getPort());
    }
}
