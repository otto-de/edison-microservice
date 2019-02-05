package de.otto.edison.mongo.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static com.mongodb.MongoCredential.createCredential;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(prefix = "edison.mongo", name = "db")
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfiguration {

    private static final Logger LOG = getLogger(MongoConfiguration.class);

    private static MongoCredential getMongoCredentials(final MongoProperties mongoProperties) {
        return createCredential(
                mongoProperties.getUser(),
                getAuthenticationDb(mongoProperties),
                mongoProperties.getPassword().toCharArray()
        );
    }

    private boolean useUnauthorizedConnection(final MongoProperties mongoProperties) {
        return mongoProperties.getUser().isEmpty() || mongoProperties.getPassword().isEmpty();
    }

    private static String getAuthenticationDb(final MongoProperties mongoProperties) {
        final String authenticationDb = mongoProperties.getAuthenticationDb();
        if (authenticationDb != null && !authenticationDb.isEmpty()) {
            return authenticationDb;
        }
        return mongoProperties.getDb();
    }

    @Bean
    @ConditionalOnMissingBean(CodecRegistry.class)
    public CodecRegistry codecRegistry() {
        return MongoClient.getDefaultCodecRegistry();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "mongoClient", value = MongoClient.class)
    public MongoClient mongoClient(final MongoProperties mongoProperties) {
        LOG.info("Creating MongoClient");

        List<ServerAddress> mongoServers = mongoProperties.getServers();
        MongoClientOptions mongoClientOptions = mongoProperties.toMongoClientOptions(codecRegistry());

        if(!useUnauthorizedConnection(mongoProperties))
        {
            return new MongoClient(mongoServers, getMongoCredentials(mongoProperties),
                    mongoClientOptions);
        }
        else {
            return new MongoClient(mongoServers,
                    mongoClientOptions);
        }
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "mongoDatabase", value = MongoDatabase.class)
    public MongoDatabase mongoDatabase(final MongoClient mongoClient, final MongoProperties mongoProperties) {
        return mongoClient.getDatabase(mongoProperties.getDb());
    }

}
