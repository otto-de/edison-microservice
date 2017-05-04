package de.otto.edison.example.configuration;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import de.otto.edison.dependencies.domain.Datasource;
import de.otto.edison.dependencies.domain.DatasourceDependency;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.dependencies.domain.Datasource.datasource;
import static de.otto.edison.dependencies.domain.DatasourceDependencyBuilder.mongoDependency;

@Configuration
public class ExampleMongoConfiguration {

    @Bean
    public MongoClient mongoClient(final MongoProperties mongoProperties) {
        return new Fongo(mongoProperties.getHost()[0]).getMongo();
    }

    @Bean
    public DatasourceDependency togglzMongoDependency(final MongoProperties mongoProperties) {
        return mongoDependency(datasourceOf(mongoProperties))
                .withName("Togglz DB")
                .withDescription("Database used to store the state of the toggles")
                .build();
    }

    private Datasource datasourceOf(final MongoProperties mongoProperties) {
        return datasource(mongoProperties.getHost()[0] + "/" + mongoProperties.getDb());
    }

}
