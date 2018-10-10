package de.otto.edison.authentication;

import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LdapRoleAuthenticationFilterTest {

    @Test
    public void shouldRejectUserThatHasNotRequiredRole() throws ServletException, IOException {
        // given
        LdapProperties ldapProperties = mockLdapPropertiesWithRequiredRole("roleX");
        LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        HttpServletRequest request = mockRequestWithAvailableRoles("roleA", "roleB");
        HttpServletResponse response = mockResponse();
        FilterChain filterChain = mockFilterChain();

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verifyZeroInteractions(filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void shouldContinueFilterChainWhenUserHasRequiredRole() throws ServletException, IOException {
        // given
        LdapProperties ldapProperties = mockLdapPropertiesWithRequiredRole("roleB");
        LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        HttpServletRequest request = mockRequestWithAvailableRoles("roleA", "roleB", "roleC");
        HttpServletResponse response = mockResponse();
        FilterChain filterChain = mockFilterChain();

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyZeroInteractions(response);
    }

    @Test
    public void shouldInvokeFilterLogicWhenRequestIsForSecuredPath() throws ServletException {
        // given
        LdapProperties ldapProperties = mockLdapPropertiesWithProtecedAndWhiteListedPath("/internal", "/internal/public");
        LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        HttpServletRequest request = mockRequestWithPath("/internal");

        // when
        boolean shouldInvokeFilterLogic = !filter.shouldNotFilter(request);

        // then
        assertTrue(shouldInvokeFilterLogic);
    }

    @Test
    public void shouldNotInvokeFilterLogicWhenRequestIsForWhitelistedPath() throws ServletException {
        // given
        LdapProperties ldapProperties = mockLdapPropertiesWithProtecedAndWhiteListedPath("/internal", "/internal/public");
        LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        HttpServletRequest request = mockRequestWithPath("/internal/public");

        // when
        boolean shouldInvokeFilterLogic = !filter.shouldNotFilter(request);

        // then
        assertFalse(shouldInvokeFilterLogic);
    }


    private HttpServletRequest mockRequestWithAvailableRoles(String... roles) {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.isUserInRole((anyString())))
                .thenAnswer((Answer) invocation -> {
                    String arg = (String) invocation.getArguments()[0];
                    return Arrays.asList(roles).contains(arg);
                });
        return httpServletRequest;
    }

    private HttpServletRequest mockRequestWithPath(String path) {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getServletPath()).thenReturn(path);
        return httpServletRequest;
    }

    private LdapProperties mockLdapPropertiesWithRequiredRole(String role) {
        LdapProperties ldapPropertiesMock = mock(LdapProperties.class);
        when(ldapPropertiesMock.getRequiredRole()).thenReturn(role);
        return ldapPropertiesMock;
    }

    private LdapProperties mockLdapPropertiesWithProtecedAndWhiteListedPath(String securedPath, String whiteListedPath) {
        LdapProperties ldapPropertiesMock = mock(LdapProperties.class);
        when(ldapPropertiesMock.getPrefixes()).thenReturn(singletonList(securedPath));
        when(ldapPropertiesMock.getWhitelistedPaths()).thenReturn(singletonList(whiteListedPath));
        when(ldapPropertiesMock.getRequiredRole()).thenReturn("someRole");
        return ldapPropertiesMock;
    }

    private FilterChain mockFilterChain() {
        return mock(FilterChain.class);
    }

    private HttpServletResponse mockResponse() {
        return mock(HttpServletResponse.class);
    }
}