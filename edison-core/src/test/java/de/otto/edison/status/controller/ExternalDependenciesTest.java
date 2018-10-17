package de.otto.edison.status.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static de.otto.edison.status.domain.Criticality.criticality;
import static de.otto.edison.status.domain.Datasource.datasource;
import static de.otto.edison.status.domain.DatasourceDependencyBuilder.mongoDependency;
import static de.otto.edison.status.domain.Expectations.lowExpectations;
import static de.otto.edison.status.domain.Level.HIGH;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.restServiceDependency;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DependenciesController.class)
@Import({WebEndpointProperties.class, EdisonApplicationProperties.class})
@ActiveProfiles("test")
public class ExternalDependenciesTest {

    @MockBean
    private ExternalDependencies externalDependencies;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnEmptyDependencies() throws Exception {
        when(externalDependencies.getDependencies()).thenReturn(emptyList());
        mockMvc.perform(get("/internal/dependencies").accept(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(
                        jsonPath("@.dependencies").isArray()
                )
                .andExpect(
                        jsonPath("@.dependencies").isEmpty()
                );
    }

    @Test
    public void shouldReturnDependencies() throws Exception {
        when(externalDependencies.getDependencies()).thenReturn(asList(
                mongoDependency(singletonList(datasource("foo:42/bar"))).withName("test").build(),
                restServiceDependency("foobar:4711").build()
        ));
        mockMvc.perform(get("/internal/dependencies").accept(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(
                        jsonPath("@.dependencies").isArray()
                )
                .andExpect(
                        jsonPath("@.dependencies[0].datasources").isArray()
                )
                .andExpect(
                        jsonPath("@.dependencies[0].datasources[0]").value("foo:42/bar")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].url").doesNotExist()
                )
                .andExpect(
                        jsonPath("@.dependencies[0].name").value("test")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].type").value("db")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].subtype").value("MongoDB")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].url").value("foobar:4711")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].type").value("service")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].subtype").value("REST")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].datasources").doesNotExist()
                )
        ;
    }

    @Test
    public void shouldReturnCriticalityAndExpectations() throws Exception {
        when(externalDependencies.getDependencies()).thenReturn(asList(
                mongoDependency(singletonList(datasource("foo:42/bar"))).build(),
                restServiceDependency("foobar:4711")
                        .withCriticality(criticality(HIGH, "Bad. Really bad."))
                        .withExpectations(lowExpectations())
                        .build()
        ));
        mockMvc.perform(get("/internal/dependencies").accept(APPLICATION_JSON))
                .andExpect(
                        jsonPath("@.dependencies[0].criticality.level").value("NOT_SPECIFIED")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].criticality.disasterImpact").value("Not Specified")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].expectations.availability").value("NOT_SPECIFIED")
                )
                .andExpect(
                        jsonPath("@.dependencies[0].expectations.performance").value("NOT_SPECIFIED")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].criticality.level").value("HIGH")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].criticality.disasterImpact").value("Bad. Really bad.")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].expectations.availability").value("LOW")
                )
                .andExpect(
                        jsonPath("@.dependencies[1].expectations.performance").value("LOW")
                );
    }

    @SpringBootApplication(scanBasePackages = "de.otto.edison.status")
    public static class TestServer {

        public static void main(String[] args) {
            run(TestServer.class, args);
        }

    }
}
