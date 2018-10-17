package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static de.otto.edison.authentication.configuration.EncryptionType.StartTLS;
import static de.otto.edison.authentication.configuration.LdapProperties.ldapProperties;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapRoleCheckingRequestTest {


    @Test
    public void shouldReturnUserRoles() throws LDAPException {
        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        final LDAPInterface ldap = someLdapInterfaceReturning("foo");

        final LdapRoleCheckingRequest request = new LdapRoleCheckingRequest(mockRequest, ldap, "uid=test", someLdapProperties());

        assertThat(request.getRoles()).contains("foo");
    }

    @Test
    public void shouldCheckUserRoles() throws LDAPException {
        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        final LDAPInterface ldap = someLdapInterfaceReturning("foo", "bar");

        final LdapRoleCheckingRequest request = new LdapRoleCheckingRequest(mockRequest, ldap, "uid=test", someLdapProperties());

        assertThat(request.isUserInRole("foo")).isEqualTo(true);
        assertThat(request.isUserInRole("foobar")).isEqualTo(false);
    }

    private LdapProperties someLdapProperties() {
        return ldapProperties("", 389, singletonList("someBaseDn"), "someRoleBaseDn", "someRdnIdentifier", "/internal", StartTLS);
    }

    private LDAPInterface someLdapInterfaceReturning(String... roles) throws LDAPSearchException {
        final LDAPInterface ldap = mock(LDAPInterface.class);
        final List<SearchResultEntry> entries = singletonList(
                new SearchResultEntry("", singleton(new Attribute("cn", roles))));
        final SearchResult searchResult = new SearchResult(0, null, null, null, null, entries, emptyList(), 1, 0, null);
        when(ldap.search(any(SearchRequest.class))).thenReturn(searchResult);
        return ldap;
    }
}