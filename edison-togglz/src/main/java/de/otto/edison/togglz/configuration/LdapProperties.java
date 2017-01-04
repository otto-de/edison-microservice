package de.otto.edison.togglz.configuration;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.toLanguageTag;

/**
 * Properties used to configure the LDAP authentication of the Togglz Console.
 */
@ConfigurationProperties(prefix = "edison.togglz.ldap-authentication")
public class LdapProperties {
    private static final Logger LOG = getLogger(LdapProperties.class);

    private boolean enabled = false;
    private String host = "";
    private int port = 389;
    private String baseDn = "";
    private String rdnIdentifier = "";

    public static LdapProperties ldapProperties(final String host, final int port, final String baseDn, final String rdnIdentifier) {
        final LdapProperties ldapProperties = new LdapProperties();
        ldapProperties.setEnabled(true);
        ldapProperties.setHost(host);
        ldapProperties.setPort(port);
        ldapProperties.setBaseDn(baseDn);
        ldapProperties.setRdnIdentifier(rdnIdentifier);
        return ldapProperties;
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
