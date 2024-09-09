package de.otto.edison.togglz.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.util.HttpServletRequestHolder;

import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserProviderTest {

    @Test
    public void shouldReturnAuthenticatedUser() {
        // given
        final UserProvider userProvider = new TogglzConfiguration().userProvider();

        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic " + Base64.getEncoder().encodeToString("testuser:passwd".getBytes()));

        HttpServletRequestHolder.bind(mockRequest);

        // when
        final FeatureUser currentUser = userProvider.getCurrentUser();

        // then
        assertThat(currentUser.getName(), is("testuser"));
    }

    @Test
    void shouldReturnNullWhenNoHttpServletRequestIsAvailable() {
        // given
        UserProvider userProvider = new TogglzConfiguration().userProvider();
        HttpServletRequestHolder.bind(null);

        // when
        FeatureUser currentUser = userProvider.getCurrentUser();

        // then
        assertThat(currentUser, is(nullValue()));
    }
}
