package de.otto.edison.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.mongodb.MongoCredential.createMongoCRCredential;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class MongoConfiguration {

    private static final Logger LOG = getLogger(MongoConfiguration.class);

    private List<ServerAddress> databaseServers;
    @Value("${edison.mongo.maxWaitTime:5000}")
    private int maxWaitTime = 5000;
    @Value("${edison.mongo.connectTimeout:5000}")
    private int connectTimeout = 5000;
    @Value("${edison.mongo.socketTimeout:2000}")
    private int socketTimeout = 10000;

    @Value("${edison.mongo.connectionpool.maxSize:100}")
    private int connectionsPerHostMax = 100;
    @Value("${edison.mongo.connectionpool.minSize:2}")
    private int connectionsPerHostMin = 2;
    @Value("${edison.mongo.blockedConnectionMultiplier:2}")
    private int blockedConnectionMultiplier = 2;
    @Value("${edison.mongo.connectionpool.maxLifeTime:100000}")
    private int maxConnectionLifeTime = 100000;
    @Value("${edison.mongo.connectionpool.maxIdleTime:10000}")
    private int maxConnectionIdleTime = 10000;
    @Value("${edison.mongo.db}")
    private String databaseName = "";
    @Value("${edison.mongo.user:}")
    private String databaseUser = "";
    @Value("${edison.mongo.passwd:}")
    private String databasePasswd;

    @Value("${edison.mongo.host:localhost}")
    public void setDatabaseServers(String[] servers) {
        this.databaseServers = Stream.of(servers)
                .filter(s -> s != null)
                .map(this::toServerAddress)
                .filter(s -> s != null)
                .collect(toList());
    }

    @Bean
    @ConditionalOnMissingBean(value = MongoClient.class)
    public MongoClient mongoClient() {
        LOG.info("Creating MongoClient");
        final MongoClientOptions settings = MongoClientOptions.builder()
                .minConnectionsPerHost(connectionsPerHostMin)
                .connectionsPerHost(connectionsPerHostMax)
                .connectTimeout(connectTimeout)
                .cursorFinalizerEnabled(true)
                .maxConnectionIdleTime(maxConnectionIdleTime)
                .maxConnectionLifeTime(maxConnectionLifeTime)
                .maxWaitTime(maxWaitTime)
                .socketTimeout(socketTimeout)
                .threadsAllowedToBlockForConnectionMultiplier(blockedConnectionMultiplier)
                .build();

        return new MongoClient(databaseServers, getMongoCredentials(), settings);
    }

    @Bean
    @ConditionalOnMissingBean(MongoDatabase.class)
    public MongoDatabase mongoDatabase() {
        return mongoClient().getDatabase(databaseName);
    }

    private List<MongoCredential> getMongoCredentials() {

        if (useUnauthorizedConnection()) {
            return Collections.emptyList();
        }
        return asList(
                createMongoCRCredential(
                        databaseUser,
                        databaseName,
                        databasePasswd.toCharArray()));
    }

    private boolean useUnauthorizedConnection() {
        return databaseUser.isEmpty() || databasePasswd.isEmpty();
    }

    private ServerAddress toServerAddress(String server) {
        try {
            if (server.contains(":")) {
                String[] hostNamePortPair = server.split(":");
                return new ServerAddress(hostNamePortPair[0], Integer.parseInt(hostNamePortPair[1]));
            } else {
                return new ServerAddress(server);
            }
        } catch (final NumberFormatException e) {
            LOG.warn("Invalid portNumber: " + e.getMessage(), e);
            return null;
        }
    }

}
