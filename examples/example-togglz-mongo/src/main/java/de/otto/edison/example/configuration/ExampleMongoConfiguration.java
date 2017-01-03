package de.otto.edison.example.configuration;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ExampleMongoConfiguration {

    @Bean
    public MongoDatabase mongoDatabase() {
        return new Fongo("localDB").getDatabase("someInMemoryDB");
    }

}
