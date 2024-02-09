package de.otto.edison.env;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParamStorePropertySourcePostProcessorTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({SSMTestConfiguration.class, ParamStoreConfiguration.class})
    private static class ParamStoreTestConfiguration {
    }

    @TestConfiguration
    static class SSMTestConfiguration {
        @Bean
        public SsmClient ssmClient() {
            SsmClient mock = mock(SsmClient.class);
            when(mock.getParametersByPath(any(GetParametersByPathRequest.class))).thenReturn(
                    GetParametersByPathResponse.builder()
                            .parameters(
                                    Parameter.builder().name("mongo/password").value("secret").build(),
                                    Parameter.builder().name("mongo/subSection/config").value("someConfig").build()
                            )
                            .build()
            );
            return mock;
        }

    }

    @AfterEach
    void close() {
        this.context.close();
    }

    @Test
    void shouldLoadPropertiesFromParamStore() {
        this.context.register(ParamStoreTestConfiguration.class);
        TestPropertyValues
                .of("edison.env.paramstore.enabled=true")
                .and("edison.env.paramstore.path=mongo")
                .applyTo(context);
        this.context.refresh();
        assertThat(this.context.containsBean("paramStorePropertySourcePostProcessor"), is(true));
        assertThat(this.context.getEnvironment().getPropertySources().contains("paramStorePropertySource"), is(true));
        assertEquals("secret", this.context.getEnvironment().getProperty("password"));
        assertEquals("someConfig", this.context.getEnvironment().getProperty("subSection/config"));
    }

    @Test
    void shouldLoadPropertiesWithSeperatorsFromParamStore() {
        this.context.register(ParamStoreTestConfiguration.class);
        TestPropertyValues
                .of("edison.env.paramstore.enabled=true")
                .and("edison.env.paramstore.path=mongo")
                .and("edison.env.paramstore.separator=/")
                .applyTo(context);
        this.context.refresh();
        assertThat(this.context.containsBean("paramStorePropertySourcePostProcessor"), is(true));
        assertThat(this.context.getEnvironment().getPropertySources().contains("paramStorePropertySource"), is(true));
        assertEquals("someConfig", this.context.getEnvironment().getProperty("subSection.config"));
    }

    @Test
    void shouldNotLoadPropertiesFromParamStore() {
        TestPropertyValues
                .of("edison.env.paramstore.enabled=false")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("paramStorePropertySourcePostProcessor"), is(false));
    }
}