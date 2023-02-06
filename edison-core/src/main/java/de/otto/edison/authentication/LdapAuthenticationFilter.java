package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import de.otto.edison.authentication.configuration.LdapProperties;
import de.otto.edison.authentication.connection.LdapConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static de.otto.edison.authentication.Credentials.readFrom;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Filter that checks for LDAP authentication once per request. Will not filter routes starting with
 * {@link LdapProperties#whitelistedPaths}. Uses {@link LdapProperties} to create an SSL based connection to the
 * configured LDAP server. Rejects requests with {@code HTTP 401} if authorization fails.
 */
public class LdapAuthenticationFilter extends OncePerRequestFilter {


    private static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    private final LdapProperties ldapProperties;
    private final LdapConnectionFactory ldapConnectionFactory;

    public LdapAuthenticationFilter(final LdapProperties ldapProperties, final LdapConnectionFactory ldapConnectionFactory) {
        if (!ldapProperties.isValid()) {
            throw new IllegalStateException("Invalid LdapProperties");
        }
        this.ldapProperties = ldapProperties;
        this.ldapConnectionFactory = ldapConnectionFactory;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        final String servletPath = request.getServletPath();
        return ldapProperties.getWhitelistedPaths()
                .stream()
                .anyMatch(servletPath::startsWith);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        Optional<Credentials> optionalCredentials = readFrom(request);
        if (optionalCredentials.isPresent()) {
            final Optional<HttpServletRequest> authRequest = tryToGetAuthenticatedRequest(request, optionalCredentials.get());
            if (authRequest.isPresent()) {
                filterChain.doFilter(authRequest.get(), response);
            } else {
                unauthorized(response);
            }
        } else {
            unauthorized(response);
        }
    }

    private Optional<HttpServletRequest> tryToGetAuthenticatedRequest(final HttpServletRequest request, final Credentials credentials) {
        try (final LDAPConnection ldap = ldapConnectionFactory.buildLdapConnection()) {

            for (String baseDN : ldapProperties.getBaseDn()) {
                final String userDN = userDnFrom(credentials, baseDN);
                try {
                    if (authenticate(ldap, userDN, credentials.getPassword())) {
                        return ldapProperties.getRoleBaseDn() != null
                                ? Optional.of(new LdapRoleCheckingRequest(request, ldap, userDN, ldapProperties))
                                : Optional.of(request);
                    }
                } catch (LDAPBindException e) {
                    LOG.debug("LDAPBindException for userDN: {}", userDN);
                }
            }
            LOG.warn("Could not bind to LDAP: {}", credentials.getUsername());
        } catch (LDAPException | GeneralSecurityException e) {
            LOG.warn("Authentication error: ", e);
        }
        return Optional.empty();
    }

    void unauthorized(final HttpServletResponse httpResponse) {
        httpResponse.addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
        httpResponse.setStatus(UNAUTHORIZED.value());
    }

    String userDnFrom(final Credentials credentials, String baseDN) {
        return format("%s=%s,%s", ldapProperties.getRdnIdentifier(), credentials.getUsername(), baseDN);
    }

    boolean authenticate(final LDAPConnection ldap, final String userDN, final String password) throws LDAPException {
        final BindResult bindResult = ldap.bind(userDN, password);
        if (bindResult.getResultCode().equals(ResultCode.SUCCESS)) {
            LOG.debug("Login successful: " + userDN); // don't expose user names at successful login as this is a security issue
            return true;
        } else {
            LOG.warn("Access denied: " + userDN);
            return false;
        }
    }
}
