package de.otto.edison.registry.client;

import de.otto.edison.registry.security.OAuth2TokenProviderFactory;
import de.otto.edison.status.configuration.ApplicationInfoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncHttpRegistryClientTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    public void setUp() {
        context = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void shouldDoNothingIfNotEnabled() {
        // given
        TestPropertyValues.of("edison.serviceregistry.enabled=false").applyTo(context);
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        // when
        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldHaveRegistryIfServersAndServicePresent() {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.servers=http://foo")
                .and("edison.serviceregistry.service=http://test")
                .applyTo(context);
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        assertThat(context.containsBean("asyncHttpRegistryClient"), is(true));
    }

    @Test
    public void shouldDoNothingIfNoServersAreSet() {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=true")
                .and("edison.serviceregistry.servers=")
                .applyTo(context);
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNoServiceAreSet() {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=true")
                .and("edison.serviceregistry.service=")
                .applyTo(context);
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfRegistryDisabled() {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=false")
                .and("edison.serviceregistry.servers=http://foo")
                .and("edison.serviceregistry.service=http://test")
                .applyTo(context);
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNothingConfigured() {
        // given
        context.register(ApplicationInfoConfiguration.class);
        context.register(OAuth2TokenProviderFactory.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }
}
