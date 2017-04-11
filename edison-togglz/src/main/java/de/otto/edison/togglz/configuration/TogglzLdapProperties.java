package de.otto.edison.togglz.configuration;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Properties used to configure the LDAP authentication of the Togglz Console.
 */
@ConfigurationProperties(prefix = "edison.togglz.console.ldap")
@Validated
public class TogglzLdapProperties {

    private static final Logger LOG = getLogger(TogglzLdapProperties.class);

    /**
     * Enable / disable the LDAP authentication for Togglz web console
     */
    private boolean enabled = false;
    /**
     * LDAP server
     */
    @NotEmpty
    private String host;
    /**
     * Port of the LDAP server
     */
    @Min(1)
    private int port = 389;
    /**
     * Base distinguished name (base DN)
     */
    @NotEmpty
    private String baseDn;
    /**
     * Relative distinguished name (RDN)
     */
    @NotEmpty
    private String rdnIdentifier;

    /**
     * Creates Ldap properties. Primarily used in tests.
     *
     * @param host LDAP server
     * @param port LDAP port
     * @param baseDn Base distinguished name
     * @param rdnIdentifier Relative distinguished name
     * @return Ldap properties
     */
    public static TogglzLdapProperties ldapProperties(final String host, final int port, final String baseDn, final String rdnIdentifier) {
        final TogglzLdapProperties ldap = new TogglzLdapProperties();
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
