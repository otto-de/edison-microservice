package de.otto.edison.authentication.connection;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import java.security.GeneralSecurityException;

public interface LdapConnectionFactory {

    LDAPConnection buildLdapConnection() throws GeneralSecurityException, LDAPException;
}
