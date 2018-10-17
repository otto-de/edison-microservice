package de.otto.edison.togglz.configuration;

import de.otto.edison.navigation.NavBarConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TogglzConsoleConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @AfterEach
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldRegisterTogglzConsoleServlet() {
        this.context.register(TogglzConsoleConfiguration.class);
        this.context.register(NavBarConfiguration.class);
        TestPropertyValues.of("edison.application.management.base-path=/internal").applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("togglzServlet"), is(true));
    }

    @Test
    public void shouldNotRegisterTogglzConsoleServletIfDisabled() {
        this.context.register(TogglzConsoleConfiguration.class);
        this.context.register(NavBarConfiguration.class);
        TestPropertyValues.of("edison.application.management.base-path=/internal", "edison.togglz.console.enabled=false").applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("togglzServlet"), is(false));
    }

}
