package de.otto.edison.authentication;

import com.unboundid.ldap.sdk.*;
import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.unboundid.ldap.sdk.ResultCode.AUTHORIZATION_DENIED;
import static com.unboundid.ldap.sdk.ResultCode.SUCCESS;
import static de.otto.edison.authentication.configuration.LdapProperties.ldapProperties;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class LdapAuthenticationFilterTest {

    private static final String WHITELISTED_PATH = "/internal/health";

    private LdapAuthenticationFilter testee;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        testee = new LdapAuthenticationFilter(ldapProperties("someHost", 389, singletonList("someBaseDn"), null, "someRdnIdentifier", "/internal", WHITELISTED_PATH));
        response = mock(HttpServletResponse.class);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToStartIfHostIsNotConfigured() throws Exception {
        new LdapAuthenticationFilter(ldapProperties("", 389, singletonList("someBaseDn"), null, "someRdnIdentifier", "/internal"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToStartIfBaseDnIsNotConfigured() throws Exception {
        new LdapAuthenticationFilter(ldapProperties("someHost", 389, singletonList(""), null, "someRdnIdentifier", "/internal"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToStartIfRdnIdentifierIsNotConfigured() throws Exception {
        new LdapAuthenticationFilter(ldapProperties("someHost", 389, singletonList("someBaseDn"), null, "", "/internal"));
    }

    @Test
    public void shouldFailToStartIfAuthorizationHeaderIsMissing() throws Exception {
        testee.doFilter(requestWithoutAuthorizationHeader(), response, mock(FilterChain.class));
        assertUnauthorized();
    }

    @Test
    public void shouldBeUnauthenticatedIfLdapConnectionFails() throws Exception {
        testee.doFilter(requestWithAuthorizationHeader(), response, mock(FilterChain.class));
        assertUnauthorized();
    }

    @Test
    public void shouldNotApplyFilterToWhitelistedEndpoint() throws Exception {
        HttpServletRequest request = requestWithoutAuthorizationHeader();
        when(request.getServletPath()).thenReturn(WHITELISTED_PATH + "/etc");

        FilterChain filterChain = mock(FilterChain.class);
        testee.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldNotApplyFilterToInternalJavascript() throws Exception {
        HttpServletRequest request = requestWithoutAuthorizationHeader();
        when(request.getServletPath()).thenReturn("/internal/js/foo.js");

        FilterChain filterChain = mock(FilterChain.class);
        testee.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldApplyFilterToAuthenticatedUser() throws IOException, ServletException, GeneralSecurityException, LDAPException {
        final LdapProperties ldapProperties = ldapProperties("someHost", 389, singletonList("someBaseDn"), null, "someRdnIdentifier", "/internal", WHITELISTED_PATH);
        final LdapConnectionFactory connectionFactory = mock(LdapConnectionFactory.class);
        final LDAPConnection ldapConnection = someLdapConnectionReturning(SUCCESS);
        when(connectionFactory.buildLdapConnection()).thenReturn(ldapConnection);
        testee = new LdapAuthenticationFilter(ldapProperties, connectionFactory);
        final HttpServletRequest request = requestWithAuthorizationHeader();
        when(request.getServletPath()).thenReturn("/foo");
        FilterChain filterChain = mock(FilterChain.class);
        testee.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void shouldApplyFilterToAuthenticatedUserWithAdditionallyConfiguredBaseDn() throws IOException, ServletException, GeneralSecurityException, LDAPException {
        // given
        final LdapProperties ldapProperties = ldapProperties("someHost", 389, asList("exceptionBaseDn", "successBaseDn"), null, "someRdnIdentifier", "/internal", WHITELISTED_PATH);
        final LdapConnectionFactory connectionFactory = mock(LdapConnectionFactory.class);
        final LDAPConnection ldapConnection = someLdapConnectionReturningSuccessOrThrowingBindException("successBaseDn", "exceptionBaseDn");
        when(connectionFactory.buildLdapConnection()).thenReturn(ldapConnection);
        testee = new LdapAuthenticationFilter(ldapProperties, connectionFactory);
        // when
        final HttpServletRequest request = requestWithAuthorizationHeader();
        when(request.getServletPath()).thenReturn("/foo");
        FilterChain filterChain = mock(FilterChain.class);
        testee.doFilter(request, response, filterChain);
        // then
        verify(ldapConnection).bind(contains("exceptionBaseDn"), anyString());
        verify(ldapConnection).bind(contains("successBaseDn"), anyString());
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void shouldNotApplyFilterToNotAuthenticatedUser() throws IOException, ServletException, GeneralSecurityException, LDAPException {
        final LdapProperties ldapProperties = ldapProperties("someHost", 389, singletonList("someBaseDn"), null, "someRdnIdentifier", "/internal", WHITELISTED_PATH);
        final LdapConnectionFactory connectionFactory = mock(LdapConnectionFactory.class);
        final LDAPConnection ldapConnection = someLdapConnectionReturning(AUTHORIZATION_DENIED);
        when(connectionFactory.buildLdapConnection()).thenReturn(ldapConnection);
        testee = new LdapAuthenticationFilter(ldapProperties, connectionFactory);
        final HttpServletRequest request = requestWithAuthorizationHeader();
        when(request.getServletPath()).thenReturn("/foo");
        FilterChain filterChain = mock(FilterChain.class);
        testee.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void shouldAuthenticateUser() throws LDAPException {
        boolean authenticated = testee.authenticate(someLdapConnectionReturning(SUCCESS), "user", "password");
        assertThat(authenticated).isEqualTo(true);
    }

    @Test
    public void shouldNotAuthenticateUser() throws LDAPException {
        boolean authenticated = testee.authenticate(someLdapConnectionReturning(AUTHORIZATION_DENIED), "user", "password");
        assertThat(authenticated).isEqualTo(false);
    }

    @Test
    public void shouldBuildUserDnFromCredentials() {
        final String userDn = testee.userDnFrom(new Credentials("user", "password"), "someBaseDn");
        assertThat(userDn).isEqualTo("someRdnIdentifier=user,someBaseDn");
    }

    private HttpServletRequest requestWithoutAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/internal");
        return request;
    }

    private HttpServletRequest requestWithAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic " + Base64Utils.encodeToString("someUsername:somePassword".getBytes()));
        when(request.getServletPath()).thenReturn("/internal");
        return request;
    }

    private void assertUnauthorized() {
        verify(response).setStatus(UNAUTHORIZED.value());
        verify(response).addHeader(WWW_AUTHENTICATE, "Basic realm=Authorization Required");
    }

    private LDAPConnection someLdapConnectionReturning(final ResultCode resultCode) throws LDAPException {
        final LDAPConnection ldap = mock(LDAPConnection.class);
        final BindResult mockBindResult = mock(BindResult.class);
        when(mockBindResult.getResultCode()).thenReturn(resultCode);
        when(ldap.bind(anyString(), anyString())).thenReturn(mockBindResult);
        return ldap;
    }

    private LDAPConnection someLdapConnectionReturningSuccessOrThrowingBindException(String bindDnSuccess, String bindDnException) throws LDAPException {
        final LDAPConnection ldap = mock(LDAPConnection.class);

        final BindResult mockBindResultSuccess = mock(BindResult.class);
        when(mockBindResultSuccess.getResultCode()).thenReturn(ResultCode.SUCCESS);
        when(ldap.bind(contains(bindDnSuccess), anyString())).thenReturn(mockBindResultSuccess);

        final BindResult mockBindResultInvalid = mock(BindResult.class);
        when(mockBindResultInvalid.getResultCode()).thenReturn(ResultCode.INVALID_CREDENTIALS);
        final LDAPBindException mockBindException = mock(LDAPBindException.class);
        when(mockBindException.getBindResult()).thenReturn(mockBindResultInvalid);
        when(ldap.bind(contains(bindDnException), anyString())).thenThrow(mockBindException);
        return ldap;
    }

}