package de.otto.edison.togglz.activation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.servlet.util.HttpServletRequestHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EdisonHeaderActivationStrategyTest {

    private FeatureUser user;
    private FeatureState state;
    private HttpServletRequest request;
    private final EdisonHeaderActivationStrategy strategy = new EdisonHeaderActivationStrategy();

    @BeforeEach
    public void setUp() {
        user = new SimpleFeatureUser("ea", false);
        state = new FeatureState(MyFeature.FEATURE).enable();
        request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(request, response);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
    }

    private enum MyFeature implements Feature {
        FEATURE
    }

    @Test
    public void shouldNotBeActiveWithANullRequestObject() {
        HttpServletRequestHolder.release();

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWithNoHeaders() {
        when(request.getHeader(any())).thenReturn(null);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenOnlyNonMatchingParametersArePresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("somethingThatDoesNotMatch", new String[]{"true"});
        parameters.put("somethingElse", new String[]{"aValue"});
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenRequestHeaderDoesNotIncludeFeatureToggle() {
        when(request.getHeader("X-Features")).thenReturn("OTHERFEATURE,ANOTHERFEATURE");

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldBeActiveWhenRequestHeaderIncludesFeatureToggle() {
        when(request.getHeader("X-Features")).thenReturn("FEATURE,ANOTHERFEATURE");

        boolean isActive = strategy.isActive(state, user);

        assertTrue(isActive);
    }
}
