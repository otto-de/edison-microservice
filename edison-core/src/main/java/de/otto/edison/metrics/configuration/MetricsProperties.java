package de.otto.edison.metrics.configuration;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;

/**
 * Configuration properties to configure reporting of edison-core metrics.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.metrics")
public class MetricsProperties {
    private Graphite graphite;
    private Slf4j slf4j;

    public Graphite getGraphite() {
        return graphite;
    }

    public void setGraphite(Graphite graphite) {
        this.graphite = graphite;
    }

    public Slf4j getSlf4j() {
        return slf4j;
    }

    public void setSlf4j(Slf4j slf4j) {
        this.slf4j = slf4j;
    }

    /**
     * Configuration properties used to configure reporting of edison-core metrics using Slf4J.
     */
    public static class Slf4j {
        /**
         * The Slf4J logger used to log metrics.
         */
        @NotEmpty
        private String logger;
        /**
         * The number of minutes between logging of metrics.
         */
        @Min(1)
        private long period = 5;

        public String getLogger() {
            return logger;
        }

        public void setLogger(String logger) {
            this.logger = logger;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }
    }
    /**
     * Configuration properties used to auto-configure Graphite reporting for edison-core metrics.
     */
    public static class Graphite {

        /**
         * Hostname of the Graphite server.
         */
        @NotEmpty
        private String host;
        /**
         * Port of the Graphite server.
         */
        @Min(1)
        private int port;
        /**
         * Prefix for metrics.
         */
        @NotEmpty
        private String prefix;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
