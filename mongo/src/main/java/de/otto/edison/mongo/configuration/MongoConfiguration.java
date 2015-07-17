package de.otto.edison.mongo.configuration;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(MongoClient.class)
@ConditionalOnProperty("edison.mongo.db")
public class MongoConfiguration {

    private static final Logger LOG = getLogger(MongoConfiguration.class);

    private List<ServerAddress> databaseServers;
    @Value("${edison.mongo.maxWaitQueueSize:500}")
    private int maxWaitQueueSize = 500;
    @Value("${edison.mongo.connectTimeout:5000}")
    private int connectTimeout = 5000;
    @Value("${edison.mongo.maxWaitTime:5000}")
    private int maxWaitTime = 5000;
    @Value("${edison.mongo.readTimeout:2000}")
    private int readTimeout = 10000;

    @Value("${edison.mongo.connectionpool.maxSize:250}")
    private int connectionPoolMaxSize = 250;
    @Value("${edison.mongo.connectionpool.minSize:2}")
    private int connectionPoolMinSize = 2;
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
        final MongoClientSettings settings = MongoClientSettings.builder()
                .clusterSettings(ClusterSettings.builder()
                        .hosts(databaseServers)
                        .maxWaitQueueSize(maxWaitQueueSize)
                        .build())
                .socketSettings(SocketSettings.builder()
                        .connectTimeout(connectTimeout, MILLISECONDS)
                        .readTimeout(readTimeout, MILLISECONDS)
                        .build())
                .connectionPoolSettings(ConnectionPoolSettings.builder()
                        .maxWaitQueueSize(maxWaitQueueSize)
                        .maxSize(connectionPoolMaxSize)
                        .minSize(connectionPoolMinSize)
                        .maxConnectionLifeTime(maxConnectionLifeTime, MILLISECONDS)
                        .maxConnectionIdleTime(maxConnectionIdleTime, MILLISECONDS)
                        .build())
                .credentialList(getMongoCredentials())
                .build();
        return MongoClients.create(settings);
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
