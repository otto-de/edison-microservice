package de.otto.edison.authentication;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.Base64Utils.encodeToString;

public class CredentialsTest {

    @Test
    public void shouldBeAbleToReadCredentialsFromRequest() {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Basic " + encodeToString("someUsername:somePassword".getBytes()));

        Optional<Credentials> credentials = Credentials.readFrom(httpServletRequest);

        assertThat(credentials.isPresent(), is(true));
        assertThat(credentials.get().getUsername(), is("someUsername"));
        assertThat(credentials.get().getPassword(), is("somePassword"));
    }

    @Test
    public void shouldReturnEmptyCredentialsIfHeaderDoesNotExist() {
        Optional<Credentials> credentials = Credentials.readFrom(mock(HttpServletRequest.class));

        assertThat(credentials.isPresent(), is(false));
    }

}