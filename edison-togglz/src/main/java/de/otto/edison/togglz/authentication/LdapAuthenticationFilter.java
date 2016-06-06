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

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.StringUtils.isEmpty;

public class LdapAuthenticationFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationFilter.class);

    private final String host;
    private final int port;
    private final String baseDn;
    private final String rdnIdentifier;

    public LdapAuthenticationFilter(final String host,
                                    final int port,
                                    final String baseDn,
                                    final String rdnIdentifier) {
        this.host = host;
        this.port = port;
        this.baseDn = baseDn;
        this.rdnIdentifier = rdnIdentifier;
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
            if (!configurationIsValid() || !credentials.isPresent() || !ldapAuthentication(credentials.get())) {
                unauthorized(httpResponse);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean configurationIsValid() {
        if (isEmpty(host)) {
            LOG.error("host is undefined");
        } else if (isEmpty(baseDn)) {
            LOG.error("baseDn is undefined");
        } else if (isEmpty(rdnIdentifier)) {
            LOG.error("rdnIdentifier is undefined");
        } else {
            return true;
        }
        return false;
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

            ldapConnection = new LDAPConnection(host, port);
            ldapConnection.processExtendedOperation(extRequest);
            BindResult bindResult = ldapConnection.bind(rdnIdentifier + "=" + credentials.getUsername() + "," + baseDn, credentials.getPassword());
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
