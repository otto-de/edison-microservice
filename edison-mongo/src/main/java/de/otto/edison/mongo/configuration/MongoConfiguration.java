package de.otto.edison.mongo.configuration;

import static java.util.Collections.singletonList;

import static org.slf4j.LoggerFactory.getLogger;

import static com.mongodb.MongoCredential.createCredential;

import java.util.Collections;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfiguration {

    private static final Logger LOG = getLogger(MongoConfiguration.class);

    private static List<MongoCredential> getMongoCredentials(final MongoProperties mongoProperties) {
        if (useUnauthorizedConnection(mongoProperties)) {
            return Collections.emptyList();
        }
        return singletonList(
                createCredential(mongoProperties.getUser(), mongoProperties.getDb(), mongoProperties.getPasswd().toCharArray()));
    }

    private static boolean useUnauthorizedConnection(final MongoProperties mongoProperties) {
        return mongoProperties.getUser().isEmpty() || mongoProperties.getPasswd().isEmpty();
    }

    @Bean
    @ConditionalOnMissingBean(CodecRegistry.class)
    public CodecRegistry codecRegistry() {
        return MongoClient.getDefaultCodecRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoClient", value = MongoClient.class)
    public MongoClient mongoClient(final MongoProperties mongoProperties) {
        LOG.info("Creating MongoClient");
        return new MongoClient(mongoProperties.getServers(), getMongoCredentials(mongoProperties),
                mongoProperties.toMongoClientOptions(codecRegistry()));
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDatabase", value = MongoDatabase.class)
    public MongoDatabase mongoDatabase(final MongoClient mongoClient, final MongoProperties mongoProperties) {
        return mongoClient.getDatabase(mongoProperties.getDb());
    }
}
