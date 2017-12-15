package de.otto.edison.registry.client;

import de.otto.edison.status.configuration.ApplicationInfoConfiguration;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncHttpRegistryClientTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext();
    }

    @After
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void shouldDoNothingIfNotEnabled() throws Exception {
        // given
        TestPropertyValues.of("edison.serviceregistry.enabled=false").applyTo(context);
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        // when
        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }


    @Test
    public void shouldHaveRegistryIfServersAndServicePresent() throws Exception {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.servers=http://foo")
                .and("edison.serviceregistry.service=http://test")
                .applyTo(context);
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        assertThat(context.containsBean("asyncHttpRegistryClient"), is(true));
    }

    @Test
    public void shouldDoNothingIfNoServersAreSet() throws Exception {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=true")
                .and("edison.serviceregistry.servers=")
                .applyTo(context);
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNoServiceAreSet() throws Exception {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=true")
                .and("edison.serviceregistry.service=")
                .applyTo(context);
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfRegistryDisabled() throws Exception {
        // given
        TestPropertyValues
                .of("edison.serviceregistry.enabled=false")
                .and("edison.serviceregistry.servers=http://foo")
                .and("edison.serviceregistry.service=http://test")
                .applyTo(context);
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNothingConfigured() throws Exception {
        // given
        context.register(DefaultAsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }
}
