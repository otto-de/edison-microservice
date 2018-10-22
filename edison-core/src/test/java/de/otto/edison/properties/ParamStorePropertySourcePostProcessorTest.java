package de.otto.edison.properties;

import de.otto.edison.configuration.ParamStoreProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParamStorePropertySourcePostProcessorTest {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({SSMTestConfiguration.class, ParamStoreProperties.class, PropertiesConfiguration.class})
    private static class ParamStoreAutoConfiguration {
    }

    @TestConfiguration
    static class SSMTestConfiguration {
        @Bean
        public SsmClient ssmClient() {
            SsmClient mock = mock(SsmClient.class);
            when(mock.getParametersByPath(any(GetParametersByPathRequest.class))).thenReturn(
                    GetParametersByPathResponse.builder()
                            .parameters(Parameter.builder().name("mongo/password").value("secret").build())
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
        this.context.register(ParamStoreAutoConfiguration.class);
        TestPropertyValues
                .of("edison.aws.config.paramstore.enabled=true")
                .and("edison.aws.config.paramstore.path=mongo")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("paramStorePropertySourcePostProcessor"), Matchers.is(true));
        assertThat(this.context.getEnvironment().getPropertySources().contains("parameterStorePropertySource"), Matchers.is(true));
        assertEquals("secret", this.context.getEnvironment().getProperty("password"));
    }

    @Test
    void shouldNotLoadPropertiesFromParamStore() {
        TestPropertyValues
                .of("edison.aws.config.paramstore.enabled=false")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("paramStorePropertySourcePostProcessor"), Matchers.is(false));
    }
}