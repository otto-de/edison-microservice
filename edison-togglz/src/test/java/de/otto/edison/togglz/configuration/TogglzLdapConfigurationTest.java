package de.otto.edison.togglz.configuration;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class TogglzLdapConfigurationTest {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({TogglzLdapConfiguration.class})
    static class EnableAutoConfig {
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldRegisterLdapFilter() {
        this.context.register(EnableAutoConfig.class);
        addEnvironment(this.context,
                "management.context-path=/internal",
                "edison.togglz.console.ldap.enabled=true",
                "edison.togglz.console.ldap.host=localhost",
                "edison.togglz.console.ldap.base-dn=test-dn",
                "edison.togglz.console.ldap.rdn-identifier=test-rdn");
        this.context.refresh();

        assertThat(this.context.containsBean("togglzAuthenticationFilter"), is(true));
    }

    @Test
    public void shouldNotRegisterLdapFilterIfDisabled() {
        this.context.register(EnableAutoConfig.class);
        addEnvironment(this.context,
                "management.context-path=/internal",
                "edison.togglz.console.ldap.enabled=false");
        this.context.refresh();

        assertThat(this.context.containsBean("togglzAuthenticationFilter"), is(false));
    }

    @Test(expected = UnsatisfiedDependencyException.class)
    public void shouldValidateProperties() {
        this.context.register(EnableAutoConfig.class);
        addEnvironment(this.context,
                "management.context-path=/internal",
                "edison.togglz.console.ldap.enabled=true");

        this.context.refresh();
    }

}
