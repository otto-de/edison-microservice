package de.otto.edison.authentication.configuration;

import de.otto.edison.authentication.connection.SSLLdapConnectionFactory;
import de.otto.edison.authentication.connection.StartTlsLdapConnectionFactory;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LdapConfigurationTest {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({LdapConfiguration.class})
    static class EnableAutoConfig {
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldRegisterLdapFilterButNotLdapRoleFilter() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("ldapAuthenticationFilter"), is(true));
        assertThat(this.context.containsBean("ldapRoleAuthenticationFilter"), is(false));
    }

    @Test
    public void shouldRegisterLdapFilterAndLdapRoleFilterWhenRoleIsRequired() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.required-role=someRole")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("ldapAuthenticationFilter"), is(true));
        assertThat(this.context.containsBean("ldapRoleAuthenticationFilter"), is(true));

        int ldapFilterOrder = this.context.getBean("ldapAuthenticationFilter", FilterRegistrationBean.class).getOrder();
        int ldapRoleFilterOrder = this.context.getBean("ldapRoleAuthenticationFilter", FilterRegistrationBean.class).getOrder();
        assertThat(ldapFilterOrder < ldapRoleFilterOrder, is(true));
    }

    @Test
    public void shouldNotRegisterLdapFilterIfDisabled() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=false")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("ldapAuthenticationFilter"), is(false));
    }

    @Test(expected = UnsatisfiedDependencyException.class)
    public void shouldValidateProperties() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .applyTo(context);

        this.context.refresh();
    }

    @Test
    public void shouldUseSSLEncryptionIfConfigured() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.encryptionType=SSL")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.getBean("ldapConnectionFactory").getClass().getSimpleName(), is(SSLLdapConnectionFactory.class.getSimpleName()));
    }

    @Test
    public void shouldUseStartTLSEncryptionIfConfigured() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.encryptionType=StartTLS")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.getBean("ldapConnectionFactory").getClass().getSimpleName(), is(StartTlsLdapConnectionFactory.class.getSimpleName()));
    }

    @Test
    public void shouldUseStartTLSEncryptionAsDefault() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.getBean("ldapConnectionFactory").getClass().getSimpleName(), is(StartTlsLdapConnectionFactory.class.getSimpleName()));
    }

    @Test
    public void shouldReadNewPrefixesPropertyAndIncludeOldPrefixProperty() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.prefix=/deprecatedTestPrefix")
                .and("edison.ldap.prefixes=/newTestPrefix")
                .applyTo(context);
        this.context.refresh();

        FilterRegistrationBean<?> filterRegistrationBean = this.context.getBean("ldapAuthenticationFilter", FilterRegistrationBean.class);
        ArrayList<String> urlPatterns = new ArrayList<String>(filterRegistrationBean.getUrlPatterns());
        assertThat(urlPatterns, hasSize(2));
        assertThat(urlPatterns, containsInAnyOrder("/deprecatedTestPrefix/*", "/newTestPrefix/*"));
    }

    @Test
    public void ensureBackwardsCompatibilityForPrefixesProperty() {
        this.context.register(EnableAutoConfig.class);
        TestPropertyValues
                .of("edison.application.management.base-path=/internal")
                .and("edison.ldap.enabled=true")
                .and("edison.ldap.host=localhost")
                .and("edison.ldap.rdn-identifier=test-rdn")
                .and("edison.ldap.base-dn=test-dn")
                .and("edison.ldap.prefix=/deprecatedTestPrefix")
                .applyTo(context);
        this.context.refresh();

        FilterRegistrationBean<?> filterRegistrationBean = this.context.getBean("ldapAuthenticationFilter", FilterRegistrationBean.class);
        ArrayList<String> urlPatterns = new ArrayList<String>(filterRegistrationBean.getUrlPatterns());
        assertThat(urlPatterns, hasSize(1));
        assertThat(urlPatterns, containsInAnyOrder("/deprecatedTestPrefix/*"));
    }
}