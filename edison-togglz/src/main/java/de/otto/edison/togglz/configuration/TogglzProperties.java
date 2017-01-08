package de.otto.edison.togglz.configuration;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Configuration properties used to configure edison-togglz
 */
@ConfigurationProperties(prefix = "edison.togglz")
public class TogglzProperties {

    /**
     * Number of millis used to cache toggle state.
     */
    private int cacheTtl = 5000;
    /**
     * Enable / Disable the Togglz web console.
     */
    private Console console = new Console();

    public int getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(int cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public static class Console {
        /**
         * Enable / disable the Togglz web console.
         */
        private boolean enabled = true;

        /**
         * LDAP configuration of the Togglz console.
         */
        private Ldap ldap = new Ldap();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Ldap getLdap() {
            return ldap;
        }

        public void setLdap(Ldap ldap) {
            this.ldap = ldap;
        }

        /**
         * Properties used to configure the LDAP authentication of the Togglz Console.
         */
        public static class Ldap {
            private static final Logger LOG = getLogger(Ldap.class);

            /**
             * Enable / disable the LDAP authentication for Togglz web console
             */
            private boolean enabled = false;
            private String host;
            private int port = 389;
            private String baseDn;
            private String rdnIdentifier;

            public static Ldap ldapProperties(final String host, final int port, final String baseDn, final String rdnIdentifier) {
                final Ldap ldap = new Ldap();
                ldap.setEnabled(true);
                ldap.setHost(host);
                ldap.setPort(port);
                ldap.setBaseDn(baseDn);
                ldap.setRdnIdentifier(rdnIdentifier);
                return ldap;
            }

            public boolean isValid() {
                if (isEmpty(host)) {
                    LOG.error("host is undefined");
                } else if (isEmpty(baseDn)) {
                    LOG.error("baseDn is undefined");
                } else if (isEmpty(rdnIdentifier)) {
                    LOG.error("rdnIdentifier is undefined");
                } else {
                    return true;
                }
                return false;
            }


            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

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

            public String getBaseDn() {
                return baseDn;
            }

            public void setBaseDn(String baseDn) {
                this.baseDn = baseDn;
            }

            public String getRdnIdentifier() {
                return rdnIdentifier;
            }

            public void setRdnIdentifier(String rdnIdentifier) {
                this.rdnIdentifier = rdnIdentifier;
            }
        }
    }

}
