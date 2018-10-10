package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import de.otto.edison.authentication.configuration.LdapProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;

import static com.unboundid.ldap.sdk.SearchScope.ONE;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * A HttpServletRequest that is {@link HttpServletRequest#isUserInRole(String) checking the user role}
 * using LDAP search.
 * <p>
 *     In order to check the roles, the roleBaseDn property must be set in
 *     {@link de.otto.edison.authentication.configuration.LdapProperties}
 * </p>
 */
class LdapRoleCheckingRequest extends HttpServletRequestWrapper {

    private static final String CN = "cn";

    private final LDAPInterface ldapInterface;
    private final String userDN;
    private final String roleBaseDN;
    private final List<String> userRoles;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public LdapRoleCheckingRequest(final HttpServletRequest request,
                                   final LDAPInterface ldapInterface,
                                   final String userDN,
                                   final LdapProperties ldapProperties) throws LDAPException {
        super(request);
        this.ldapInterface = ldapInterface;
        this.userDN = userDN;
        this.roleBaseDN = ldapProperties.getRoleBaseDn();
        this.userRoles = getRoles();
    }

    /**
     * Returns true, if the role is equal to one of the LDAP groups of the user identified by userDN.
     *
     * @param role
     */
    @Override
    public boolean isUserInRole(String role) {
        return userRoles.contains(role);
    }

    List<String> getRoles() throws LDAPException {
        final SearchRequest searchRequest =
                new SearchRequest(
                        roleBaseDN,
                        ONE,
                        "(uniqueMember=" + userDN + ")",
                        CN);
        final SearchResult searchResult = ldapInterface.search(searchRequest);
        return searchResult.getSearchEntries()
                .stream()
                .flatMap(entry -> stream(entry.getAttributeValues("CN")))
                .collect(toList());
    }

}
