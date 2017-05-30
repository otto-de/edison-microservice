package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.net.ssl.SSLContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static de.otto.edison.authentication.Credentials.readFrom;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Filter that checks for LDAP authentication once per request. Will not filter routes starting with
 * {@link LdapProperties#whitelistedPaths}. Uses {@link LdapProperties} to create an SSL based connection to the
 * configured LDAP server. Rejects requests with {@code HTTP 401} if authorization fails.
 */
public class LdapAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_JS_PATH = "/internal/js/";
    private static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    private final LdapProperties ldapProperties;

    public LdapAuthenticationFilter(final LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
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
        if (isEmpty(request.getHeader("Authorization"))) {
            unauthorized(response);
        } else {
            Optional<Credentials> credentials = readFrom(request);

            if (!ldapProperties.isValid() || !credentials.isPresent() || !ldapAuthentication(credentials.get())) {
                unauthorized(response);
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    private void unauthorized(final HttpServletResponse httpResponse) {
        httpResponse.addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
        httpResponse.setStatus(UNAUTHORIZED.value());
    }

    private boolean ldapAuthentication(final Credentials credentials) {
        boolean authOK = false;
        LDAPConnection ldapConnection = null;
        try {
            SSLUtil sslUtil = new SSLUtil();
            SSLContext context = sslUtil.createSSLContext();
            ExtendedRequest extRequest = new StartTLSExtendedRequest(context);

            ldapConnection = new LDAPConnection(ldapProperties.getHost(), ldapProperties.getPort());
            ldapConnection.processExtendedOperation(extRequest);
            BindResult bindResult = ldapConnection.bind(
                    ldapProperties.getRdnIdentifier() + "=" + credentials.getUsername() + "," +
                            ldapProperties.getBaseDn(),
                    credentials.getPassword()
            );
            if (bindResult.getResultCode().equals(ResultCode.SUCCESS)) {
                LOG.info("Login successful: " + credentials.getUsername());
                authOK = true;
            } else {
                LOG.info("Access denied: " + credentials.getUsername());
            }
        } catch (LDAPException | GeneralSecurityException e) {
            LOG.info("Authentication error: ", e);
        } finally {
            if (ldapConnection != null) {
                ldapConnection.close();
            }
        }

        return authOK;
    }
}
