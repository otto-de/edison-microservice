package de.otto.edison.togglz.authentication;

import de.otto.edison.togglz.configuration.LdapProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.otto.edison.togglz.configuration.LdapProperties.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class LdapAuthenticationFilterTest {

    private LdapAuthenticationFilter testee;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        testee = new LdapAuthenticationFilter(ldapProperties("someHost", 389, "someBaseDn", "someRdnIdentifier"));
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void shouldBeUnauthenticatedIfHostIsNotConfigured() throws Exception {
        testee = new LdapAuthenticationFilter(ldapProperties("", 389, "someBaseDn", "someRdnIdentifier"));
        assertValidRequestIsUnauthorized();
    }

    @Test
    public void shouldBeUnauthenticatedIfBaseDnIsNotConfigured() throws Exception {
        testee = new LdapAuthenticationFilter(ldapProperties("someHost", 389, "", "someRdnIdentifier"));
        assertValidRequestIsUnauthorized();
    }

    @Test
    public void shouldBeUnauthenticatedIfRdnIdentifierIsNotConfigured() throws Exception {
        testee = new LdapAuthenticationFilter(ldapProperties("someHost", 389, "someBaseDn", ""));
        assertValidRequestIsUnauthorized();
    }

    @Test
    public void shouldBeUnauthenticatedIfAuthorizationHeaderIsMissing() throws Exception {
        testee.doFilter(requestWithoutAuthorizationHeader(), response, mock(FilterChain.class));
        assertUnauthorized();
    }

    @Test
    public void shouldBeUnauthenticatedIfLdapConnectionFails() throws Exception {
        testee.doFilter(requestWithAuthorizationHeader(), response, mock(FilterChain.class));
        assertUnauthorized();
    }

    private HttpServletRequest requestWithoutAuthorizationHeader() {
        return mock(HttpServletRequest.class);
    }

    private HttpServletRequest requestWithAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic " + Base64Utils.encodeToString("someUsername:somePassword".getBytes()));
        return request;
    }

    private void assertValidRequestIsUnauthorized() throws IOException, ServletException {
        testee.doFilter(requestWithAuthorizationHeader(), response, mock(FilterChain.class));

        assertUnauthorized();
    }

    private void assertUnauthorized() {
        verify(response).setStatus(UNAUTHORIZED.value());
        verify(response).addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
    }

}