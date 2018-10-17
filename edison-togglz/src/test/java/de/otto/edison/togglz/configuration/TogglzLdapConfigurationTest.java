package de.otto.edison.togglz.configuration;

import de.otto.edison.authentication.configuration.LdapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TogglzLdapConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({LdapConfiguration.class})
    static class EnableAutoConfig {
    }

    @AfterEach
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldRegisterLdapFilter() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues.of(
                "edison.application.management.base-path=/internal",
                "edison.ldap.enabled=true",
                "edison.ldap.host=localhost",
                "edison.ldap.base-dn=test-dn",
                "edison.ldap.rdn-identifier=test-rdn").applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("ldapAuthenticationFilter"), is(true));
    }

    @Test
    public void shouldNotRegisterLdapFilterIfDisabled() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues.of(
                "edison.application.management.base-path=/internal",
                "edison.ldap.enabled=false").applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("ldapAuthenticationFilter"), is(false));
    }

    @Test
    public void shouldValidateProperties() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues.of(
                "edison.application.management.base-path=/internal",
                "edison.ldap.enabled=true").applyTo(context);

        assertThrows(UnsatisfiedDependencyException.class, () -> {
            this.context.refresh();
        });
    }

}
