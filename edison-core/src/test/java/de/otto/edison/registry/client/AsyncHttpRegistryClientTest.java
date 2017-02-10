package de.otto.edison.registry.client;

import com.ning.http.client.AsyncHttpClient;
import de.otto.edison.status.configuration.ApplicationInfoConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

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
        addEnvironment(context, "edison.serviceregistry.enabled=false");
        context.register(AsyncHttpClient.class);
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
        addEnvironment(context, "edison.serviceregistry.servers=http://foo");
        addEnvironment(context, "edison.serviceregistry.service=http://test");
        context.register(AsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        assertThat(context.containsBean("asyncHttpRegistryClient"), is(true));
    }

    @Test
    public void shouldDoNothingIfNoServersAreSet() throws Exception {
        // given
        addEnvironment(context, "edison.serviceregistry.enabled=true");
        addEnvironment(context, "edison.serviceregistry.servers=");
        context.register(AsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNoServiceAreSet() throws Exception {
        // given
        addEnvironment(context, "edison.serviceregistry.enabled=true");
        addEnvironment(context, "edison.serviceregistry.service=");
        context.register(AsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfRegistryDisabled() throws Exception {
        // given
        addEnvironment(context, "edison.serviceregistry.enabled=false");
        addEnvironment(context, "edison.serviceregistry.servers=http://foo");
        addEnvironment(context, "edison.serviceregistry.service=http://test");
        context.register(AsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }

    @Test
    public void shouldDoNothingIfNothingConfigured() throws Exception {
        // given
        context.register(AsyncHttpClient.class);
        context.register(ApplicationInfoConfiguration.class);
        context.register(AsyncHttpRegistryClient.class);
        context.refresh();

        RegistryClient bean = context.getBean(RegistryClient.class);

        assertThat(bean.isRunning(), is(false));
    }
}
