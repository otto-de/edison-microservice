package de.otto.edison.mongo.configuration;

import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.mongodb.MongoClientOptions.*;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Properties used to configure MongoDB clients.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.mongo")
public class MongoProperties {
    private static final Logger LOG = getLogger(MongoProperties.class);

    /**
     * The MongoDB servers. Comma-separated list of host:port pairs.
     */
    private String[] host = {"localhost"};
    /**
     * The MongoDB database.
     */
    private String db;
    /**
     * database user name
     */
    private String user= "";
    /**
     * database user password
     */
    private String passwd= "";
    /**
     * Represents preferred replica set members to which a query or command can be sent.
     */
    private String readPreference = "primaryPreferred";
    /**
     * Maximum time that a thread will block waiting for a connection.
     */
    private int maxWaitTime = 5000;
    /**
     * Connection timeout in milliseconds. Must be &gt; 0
     */
    private int connectTimeout = 5000;
    /**
     * Socket timeout.
     */
    private int socketTimeout = 2000;
    /**
     * Sets the server selection timeout in milliseconds, which defines how long the driver will wait for server selection to
     * succeed before throwing an exception.
     */
    private int serverSelectionTimeout = 30000;
    /**
     * Connection pool properties.
     */
    private Connectionpool connectionpool = new Connectionpool();

    public List<ServerAddress> getServers() {
        return Stream.of(host)
                .filter(Objects::nonNull)
                .map(this::toServerAddress)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public String[] getHost() {
        return host;
    }

    public void setHost(String[] host) {
        this.host = host;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(String readPreference) {
        this.readPreference = readPreference;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getServerSelectionTimeout() {
        return serverSelectionTimeout;
    }

    public void setServerSelectionTimeout(int serverSelectionTimeout) {
        this.serverSelectionTimeout = serverSelectionTimeout;
    }

    public Connectionpool getConnectionpool() {
        return connectionpool;
    }

    public void setConnectionpool(Connectionpool connectionpool) {
        this.connectionpool = connectionpool;
    }

    public MongoClientOptions toMongoClientOptions(final CodecRegistry codecRegistry) {
        return builder()
                .codecRegistry(codecRegistry)
                .readPreference(ReadPreference.valueOf(readPreference))
                .connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout)
                .serverSelectionTimeout(serverSelectionTimeout)
                .cursorFinalizerEnabled(true)
                .maxWaitTime(maxWaitTime)
                .maxConnectionLifeTime(connectionpool.getMaxLifeTime())
                .threadsAllowedToBlockForConnectionMultiplier(connectionpool.getBlockedConnectionMultiplier())
                .maxConnectionIdleTime(connectionpool.getMaxIdleTime())
                .minConnectionsPerHost(connectionpool.getMinSize())
                .connectionsPerHost(connectionpool.getMaxSize())
                .build();
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

    public static class Connectionpool {
        /**
         * Maximum number of connections allowed per host.
         */
        private int maxSize = 100;
        /**
         * The minimum number of connections per host.
         */
        private int minSize = 2;
        /**
         * This multiplier, multiplied with the maxSize property, gives the maximum number of threads that may be waiting for a
         * connection to become available from the pool.
         */
        private int blockedConnectionMultiplier = 2;
        /**
         * Maximum life time for a pooled connection.
         */
        private int maxLifeTime = 100000;
        /**
         * Maximum idle time for a pooled connection.
         */
        private int maxIdleTime = 10000;

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getMinSize() {
            return minSize;
        }

        public void setMinSize(int minSize) {
            this.minSize = minSize;
        }

        public int getBlockedConnectionMultiplier() {
            return blockedConnectionMultiplier;
        }

        public void setBlockedConnectionMultiplier(int blockedConnectionMultiplier) {
            this.blockedConnectionMultiplier = blockedConnectionMultiplier;
        }

        public int getMaxLifeTime() {
            return maxLifeTime;
        }

        public void setMaxLifeTime(int maxLifeTime) {
            this.maxLifeTime = maxLifeTime;
        }

        public int getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(int maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }
    }
}
