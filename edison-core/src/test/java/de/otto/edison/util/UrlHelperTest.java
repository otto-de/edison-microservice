package de.otto.edison.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlHelperTest {

    @Test
    void shouldBuildBaseUriWithContextPathAndServletPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com:8080/someContextPath/moin"));
        when(request.getServletPath()).thenReturn("/moin");
        assertEquals("http://example.com:8080/someContextPath", UrlHelper.baseUriOf(request));
    }

    @Test
    void shouldBuildBaseUriCorrectWhenRequestServletPathIsSlashOnly() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com:8080/"));
        when(request.getServletPath()).thenReturn("/");
        assertEquals("http://example.com:8080", UrlHelper.baseUriOf(request));
    }

    @Test
    void shouldBuildBaseUriWithContextPathAndServletPathWhenRequestUrlIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(null);
        when(request.getServletPath()).thenReturn("/moin");
        assertEquals("", UrlHelper.baseUriOf(request));
    }

}