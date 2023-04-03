package de.otto.edison.authentication;

import de.otto.edison.authentication.configuration.LdapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Filter that checks for LDAP authentication with certain role once per request. Will not filter routes starting with
 * {@link LdapProperties#allowlistedPaths}. Uses {@link LdapProperties} to set required role to access secured paths.
 * Rejects requests with {@code HTTP 401} if authorization fails.
 */
public class LdapRoleAuthenticationFilter extends OncePerRequestFilter {

    private static Logger LOG = LoggerFactory.getLogger(LdapRoleAuthenticationFilter.class);

    private final Collection<String> allowlistedPaths;
    private final String requiredRole;

    public LdapRoleAuthenticationFilter(final LdapProperties ldapProperties) {
        this.allowlistedPaths = requireNonNull(ldapProperties.getAllowlistedPaths(), "white listed paths must not be null");
        this.requiredRole = requireNonNull(ldapProperties.getRequiredRole(), "required role must not be null");
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        final String servletPath = request.getServletPath();
        return allowlistedPaths
                .stream()
                .anyMatch(servletPath::startsWith);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        if (request.isUserInRole(requiredRole)) {
            LOG.debug("Found correct role for login."); // don't expose roles at successful login as this is a security issue
            filterChain.doFilter(request, response);
        } else {
            LOG.warn("Did not find correct role for login.");
            unauthorized(response);
        }
    }

    void unauthorized(final HttpServletResponse httpResponse) {
        httpResponse.addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
        httpResponse.setStatus(UNAUTHORIZED.value());
    }

}
