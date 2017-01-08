package de.otto.edison.togglz.authentication;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.servlet.Filter;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static de.otto.edison.togglz.configuration.TogglzProperties.Console.Ldap;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.StringUtils.isEmpty;

public class LdapAuthenticationFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    private final Ldap ldapProperties;

    public LdapAuthenticationFilter(final Ldap ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isEmpty(httpRequest.getHeader("Authorization"))) {
            unauthorized(httpResponse);
        } else {
            Optional<Credentials> credentials = Credentials.readFrom(httpRequest);
            if (!ldapProperties.isValid() || !credentials.isPresent() || !ldapAuthentication(credentials.get())) {
                unauthorized(httpResponse);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private void unauthorized(HttpServletResponse httpResponse) {
        httpResponse.addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
        httpResponse.setStatus(UNAUTHORIZED.value());
    }

    private boolean ldapAuthentication(Credentials credentials) {
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
