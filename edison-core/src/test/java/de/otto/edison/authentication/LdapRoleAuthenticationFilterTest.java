package de.otto.edison.authentication;

import de.otto.edison.authentication.configuration.LdapProperties;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LdapRoleAuthenticationFilterTest {

    @Test
    public void shouldRejectUserThatHasNotRequiredRole() throws ServletException, IOException {
        // given
        final LdapProperties ldapProperties = mockLdapPropertiesWithRequiredRole("roleX");
        final LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        final HttpServletRequest request = mockRequestWithAvailableRoles("roleA", "roleB");
        final HttpServletResponse response = mockResponse();
        final FilterChain filterChain = mockFilterChain();

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void shouldContinueFilterChainWhenUserHasRequiredRole() throws ServletException, IOException {
        // given
        final LdapProperties ldapProperties = mockLdapPropertiesWithRequiredRole("roleB");
        final LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        final HttpServletRequest request = mockRequestWithAvailableRoles("roleA", "roleB", "roleC");
        final HttpServletResponse response = mockResponse();
        final FilterChain filterChain = mockFilterChain();

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    public void shouldInvokeFilterLogicWhenRequestIsForSecuredPath() throws ServletException {
        // given
        final LdapProperties ldapProperties = mockLdapPropertiesWithProtecedAndAllowlistedPath("/internal", "/internal/public");
        final LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        final HttpServletRequest request = mockRequestWithPath("/internal");

        // when
        final boolean shouldInvokeFilterLogic = !filter.shouldNotFilter(request);

        // then
        assertTrue(shouldInvokeFilterLogic);
    }

    @Test
    public void shouldNotInvokeFilterLogicWhenRequestIsForAllowlistedPath() throws ServletException {
        // given
        final LdapProperties ldapProperties = mockLdapPropertiesWithProtecedAndAllowlistedPath("/internal", "/internal/public");
        final LdapRoleAuthenticationFilter filter = new LdapRoleAuthenticationFilter(ldapProperties);

        final HttpServletRequest request = mockRequestWithPath("/internal/public");

        // when
        final boolean shouldInvokeFilterLogic = !filter.shouldNotFilter(request);

        // then
        assertFalse(shouldInvokeFilterLogic);
    }


    private HttpServletRequest mockRequestWithAvailableRoles(final String... roles) {
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.isUserInRole((anyString())))
                .thenAnswer((Answer) invocation -> {
                    final String arg = (String) invocation.getArguments()[0];
                    return Arrays.asList(roles).contains(arg);
                });
        return httpServletRequest;
    }

    private HttpServletRequest mockRequestWithPath(final String path) {
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getServletPath()).thenReturn(path);
        return httpServletRequest;
    }

    private LdapProperties mockLdapPropertiesWithRequiredRole(final String role) {
        final LdapProperties ldapPropertiesMock = mock(LdapProperties.class);
        when(ldapPropertiesMock.getRequiredRole()).thenReturn(role);
        return ldapPropertiesMock;
    }

    private LdapProperties mockLdapPropertiesWithProtecedAndAllowlistedPath(final String securedPath, final String allowlistedPaths) {
        final LdapProperties ldapPropertiesMock = mock(LdapProperties.class);
        when(ldapPropertiesMock.getPrefixes()).thenReturn(singletonList(securedPath));
        when(ldapPropertiesMock.getAllowlistedPaths()).thenReturn(singletonList(allowlistedPaths));
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
