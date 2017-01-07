package de.otto.edison.example.configuration;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExampleMongoConfiguration {

    @Bean
    public MongoClient mongoClient(final MongoProperties mongoProperties) {
        return new Fongo(mongoProperties.getHost()[0]).getMongo();
    }

}
