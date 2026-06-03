package de.otto.edison.mongo.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCompressor;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.event.CommandListener;
import de.otto.edison.mongo.MongoStatusDetailIndicator;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@AutoConfiguration
@ConditionalOnProperty(prefix = "edison.mongo", name = "db")
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfiguration {

    private static final Logger LOG = getLogger(MongoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(CodecRegistry.class)
    public CodecRegistry codecRegistry() {
        return MongoClientSettings.getDefaultCodecRegistry();
    }

    @ConditionalOnClass(name = "com.github.luben.zstd.Zstd")
    @Bean
    public MongoCompressor zsdtCompressor() {
        return MongoCompressor.createZstdCompressor();
    }

    @ConditionalOnClass(name = "org.xerial.snappy.Snappy")
    @Bean
    public MongoCompressor snappyCompressor() {
        return MongoCompressor.createSnappyCompressor();
    }

    @Bean
    public MongoCompressor zlibCompressor() {
        return MongoCompressor.createZlibCompressor();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "mongoClient", value = MongoClient.class)
    public MongoClient mongoClient(final MongoProperties mongoProperties,
                                   final CodecRegistry codecRegistry,
                                   final List<MongoCompressor> possibleCompressors,
                                   final List<CommandListener> commandListeners) {
        LOG.info("Creating MongoClient");
        MongoClientSettings mongoClientSettings = mongoProperties.toMongoClientSettings(codecRegistry, possibleCompressors, commandListeners);
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "mongoDatabase", value = MongoDatabase.class)
    public MongoDatabase mongoDatabase(final MongoClient mongoClient, final MongoProperties mongoProperties) {
        return mongoClient.getDatabase(mongoProperties.getDb());
    }

    @Bean
    @ConditionalOnBean(MongoDatabase.class)
    @ConditionalOnProperty(prefix = "edison.mongo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MongoStatusDetailIndicator mongoStatusDetailIndicator(final MongoDatabase mongoDatabase) {
        return new MongoStatusDetailIndicator(mongoDatabase);
    }

}
