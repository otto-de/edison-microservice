package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.*;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private static final String INTERNAL_JS_PATH = "/internal/js/";
    private static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    private final LdapProperties ldapProperties;
    private final LdapConnectionFactory connectionFactory;

    public LdapAuthenticationFilter(final LdapProperties ldapProperties) {
        if (!ldapProperties.isValid()) {
            throw new IllegalStateException("Invalid LdapProperties");
        }
        this.ldapProperties = ldapProperties;
        this.connectionFactory = new LdapConnectionFactory(ldapProperties);
    }

    public LdapAuthenticationFilter(final LdapProperties ldapProperties, final LdapConnectionFactory connectionFactory) {
        if (!ldapProperties.isValid()) {
            throw new IllegalStateException("Invalid LdapProperties");
        }
        this.ldapProperties = ldapProperties;
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        final String servletPath = request.getServletPath();
        return servletPath.startsWith(INTERNAL_JS_PATH) || ldapProperties.getWhitelistedPaths()
                .stream()
                .anyMatch(servletPath::startsWith);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        Optional<Credentials> optionalCredentials = readFrom(request);
        if (optionalCredentials.isPresent()) {
            Optional<HttpServletRequest> authRequest = tryToGetAuthenticatedRequest(request, optionalCredentials.get());
            if (authRequest.isPresent()) {
                filterChain.doFilter(authRequest.get(), response);
            } else {
                unauthorized(response);
            }
        } else {
            unauthorized(response);
        }
    }

    private Optional<HttpServletRequest> tryToGetAuthenticatedRequest(HttpServletRequest request, Credentials credentials) {
        try (final LDAPConnection ldap = connectionFactory.buildLdapConnection()) {

            for (String baseDN : ldapProperties.getBaseDn()) {
                final String userDN = userDnFrom(credentials, baseDN);
                try {
                    if (authenticate(ldap, userDN, credentials.getPassword())) {
                        return ldapProperties.getRoleBaseDn() != null
                                ? Optional.of(new LdapRoleCheckingRequest(request, ldap, userDN, ldapProperties))
                                : Optional.of(request);
                    }
                } catch (LDAPBindException e) {
                    LOG.info("LDAPBindException for userDN: {}", userDN);
                }
            }
            LOG.warn("Could not bind to LDAP: {}", credentials.getUsername());
        }  catch (LDAPException | GeneralSecurityException e) {
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
            LOG.info("Login successful: " + userDN);
            return true;
        } else {
            LOG.warn("Access denied: " + userDN);
            return false;
        }
    }
}
