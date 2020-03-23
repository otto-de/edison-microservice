package de.otto.edison.acceptance.togglz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.testsupport.togglz.FeatureManagerSupport;
import de.otto.edison.togglz.TestFeatures;
import de.otto.edison.togglz.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import java.io.IOException;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestServer.class})
@ContextConfiguration(classes = {FeatureTogglesControllerAcceptanceTest.TogglzConfiguration.class})
@ActiveProfiles("test")
public class FeatureTogglesControllerAcceptanceTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeatureManager featureManager;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        FeatureManagerSupport.allEnabledFeatureConfig(featureManager);
    }

    @Test
    public void shouldReturnTogglesAsJson() {
        // when
        ResponseEntity<String> resource = getResource("http://localhost:" + port + "/togglztest/internal/toggles");

        // then
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE/description").asText(), is("a test feature toggle"));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE/enabled").asBoolean(), is(true));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE_2/description").asText(), is("TEST_FEATURE_2"));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE_2/enabled").asBoolean(), is(true));
    }

    ResponseEntity<String> getResource(final String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                url,
                GET,
                new HttpEntity<>("parameters", headers), String.class
        );
    }

    JsonNode jsonNode(ResponseEntity<String> resource) {
        try {
            return objectMapper.readTree(resource.getBody());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Configuration
    static class TogglzConfiguration{

        @Bean
        @Profile("test")
        public TogglzConfig togglzConfig() {
            return new TogglzConfig() {
                @Override
                public Class<? extends Feature> getFeatureClass() {
                    return TestFeatures.class;
                }

                @Override
                public StateRepository getStateRepository() {
                    return new InMemoryStateRepository();
                }

                @Override
                public UserProvider getUserProvider() {
                    return new NoOpUserProvider();
                }
            };
        }

    }
}
