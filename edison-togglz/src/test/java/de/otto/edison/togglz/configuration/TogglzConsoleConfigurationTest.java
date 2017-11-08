package de.otto.edison.togglz.configuration;

import de.otto.edison.navigation.NavBarConfiguration;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class TogglzConsoleConfigurationTest {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldRegisterTogglzConsoleServlet() {
        this.context.register(TogglzConsoleConfiguration.class);
        this.context.register(NavBarConfiguration.class);
        addEnvironment(this.context, "management.endpoints.web.base-path=/internal");
        this.context.refresh();

        assertThat(this.context.containsBean("togglzServlet"), is(true));
    }

    @Test
    public void shouldNotRegisterTogglzConsoleServletIfDisabled() {
        this.context.register(TogglzConsoleConfiguration.class);
        this.context.register(NavBarConfiguration.class);
        addEnvironment(this.context, "management.endpoints.web.base-path=/internal", "edison.togglz.console.enabled=false");
        this.context.refresh();

        assertThat(this.context.containsBean("togglzServlet"), is(false));
    }

}
