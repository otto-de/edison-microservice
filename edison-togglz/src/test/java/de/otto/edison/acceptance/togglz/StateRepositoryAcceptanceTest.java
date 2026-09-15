package de.otto.edison.acceptance.togglz;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.configuration.MongoProperties;
import de.otto.edison.togglz.TestServer;
import de.otto.edison.togglz.mongo.MongoTogglzRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.togglz.core.repository.StateRepository;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {TestServer.class, ExampleMongoConfiguration.class},
        properties = {
                "edison.togglz.mongo.enabled=true",
                "mongo.db=edison-example",
                "mongo.host=localhost",
                "mongo.passwd=example",
                "mongo.user=edison-example"
        })
@ActiveProfiles("test")
@AutoConfiguration
public class StateRepositoryAcceptanceTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.5");

    @Bean
    MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase("togglz-acceptance-test");
    }

    @Bean
    MongoProperties mongoProperties() {
        return new MongoProperties();
    }

    @Autowired
    MongoClient mongoClient;

    @Autowired
    StateRepository stateRepository;

    @Test
    public void shouldUseMongoStateRepository() {
        // then
        assertThat(stateRepository.getClass(), is(MongoTogglzRepository.class));
    }

}