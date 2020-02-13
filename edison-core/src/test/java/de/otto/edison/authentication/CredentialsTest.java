package de.otto.edison.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.util.Base64Utils.encodeToString;

public class CredentialsTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private void mockHttpServletRequestWithAuthentication(final String authString) {
        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Basic " + encodeToString(authString.getBytes()));
    }

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldBeAbleToReadCredentialsFromRequest() {
        // given
        mockHttpServletRequestWithAuthentication("someUsername:somePassword");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(true));
        assertThat(credentials.get().getUsername(), is("someUsername"));
        assertThat(credentials.get().getPassword(), is("somePassword"));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfHeaderDoesNotExist() {
        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfPasswordNotSet() {
        // given
        mockHttpServletRequestWithAuthentication("someUsername:");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfUsernameNotSet() {
        // given
        mockHttpServletRequestWithAuthentication(":password");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfAnotherAuthorizationSchemeThanBasicIsUsed() {
        // given
        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn(
                        "Token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }

    @Test
    public void shouldReturnCorrectCredentialsIfPasswordContainsColons() {
        // given
        mockHttpServletRequestWithAuthentication("user:pass:word");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(true));
        assertThat(credentials.get().getUsername(), is("user"));
        assertThat(credentials.get().getPassword(), is("pass:word"));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfColonDoesNotExist() {
        // given
        mockHttpServletRequestWithAuthentication("userpass");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfAuthenticationNotBasic() {
        // given
        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer someToken");

        // when
        final Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        // then
        assertThat(credentials.isPresent(), is(false));
    }
}