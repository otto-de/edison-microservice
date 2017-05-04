package de.otto.edison.dependencies.controller;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.ServiceSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.otto.edison.dependencies.domain.Datasource.datasource;
import static de.otto.edison.dependencies.domain.DatasourceDependencyBuilder.mongoDependency;
import static de.otto.edison.dependencies.domain.ServiceDependencyBuilder.restServiceDependency;
import static de.otto.edison.status.configuration.ApplicationInfoProperties.applicationInfoProperties;
import static de.otto.edison.status.configuration.VersionInfoProperties.versionInfoProperties;
import static de.otto.edison.status.domain.ApplicationInfo.applicationInfo;
import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.ClusterInfo.clusterInfo;
import static de.otto.edison.status.domain.VersionInfo.versionInfo;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DependenciesController.class)
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
                );
    }

    private ApplicationStatus someApplicationStatus(final List<ServiceSpec> serviceSpecs) {
        return applicationStatus(
                applicationInfo("test", applicationInfoProperties("test", "test", "test", "")),
                clusterInfo("", "staged"),
                null,
                versionInfo(versionInfoProperties("", "", "")),
                null,
                emptyList(),
                serviceSpecs
        );
    }

    @SpringBootApplication(scanBasePackages = "de.otto.edison.dependencies")
    public static class TestServer {

        public static void main(String[] args) {
            run(TestServer.class, args);
        }

    }
}
