package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.*;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;

import static com.unboundid.ldap.sdk.SearchScope.SUB;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A HttpServletRequest that is {@link HttpServletRequest#isUserInRole(String) checking the user role}
 * using LDAP search.
 * <p>
 *     In order to check the roles, the roleBaseDn property must be set in
 *     {@link de.otto.edison.authentication.configuration.LdapProperties}
 * </p>
 */
class LdapRoleCheckingRequest extends HttpServletRequestWrapper {

    private static final Logger LOG = getLogger(LdapRoleCheckingRequest.class);
    private static final String CN = "cn";

    private final LDAPInterface ldapInterface;
    private final String userDN;
    private final String roleBaseDN;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public LdapRoleCheckingRequest(final HttpServletRequest request,
                                   final LDAPInterface ldapInterface,
                                   final String userDN,
                                   final LdapProperties ldapProperties) {
        super(request);
        this.ldapInterface = ldapInterface;
        this.userDN = userDN;
        this.roleBaseDN = ldapProperties.getRoleBaseDn(); //"ou=mesos,ou=groups,dc=lhotse,dc=otto,dc=de";

    }

    /**
     * Returns true, if the role is equal to one of the LDAP groups of the user identified by userDN.
     *
     * @param role
     */
    @Override
    public boolean isUserInRole(String role) {
        try {
            return getRoles().contains(role);
        } catch (LDAPException e) {
            LOG.error("Unable to retrieve user groups: " + e.getMessage(), e);
            return false;
        }
    }

    List<String> getRoles() throws LDAPException {
        final SearchRequest searchRequest =
                new SearchRequest(
                        roleBaseDN,
                        SUB,
                        "(uniqueMember=" + userDN + ")",
                        CN);
        final SearchResult searchResult = ldapInterface.search(searchRequest);
        return searchResult.getSearchEntries()
                .stream()
                .flatMap(entry -> stream(entry.getAttributeValues("CN")))
                .collect(toList());
    }
}
