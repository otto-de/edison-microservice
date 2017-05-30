package de.otto.edison.authentication.configuration;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Properties used to configure LDAP authentication.
 */
@ConfigurationProperties(prefix = "edison.ldap")
@Validated
public class LdapProperties {

    private static final Logger LOG = getLogger(LdapProperties.class);
/**
 * Add an authentication filter to the web application context if edison.ldap property is set to {@code enabled}'.
 * All routes starting with the value of the {@code edison.ldap.prefix} property will be secured by LDAP. If no
 * property is set this will default to all routes starting with '/internal'.
 */
    /**
     * Enable / disable the LDAP authentication
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
     * Prefix for LDAP secured paths, defaults to "/internal"
     */
    private String prefix = "/internal";

    /**
     * List of paths that should be whitelisted from LDAP authentication (sub-paths will also be whitelisted)
     */
    private List<String> whitelistedPaths = Collections.singletonList("/internal/health");

    /**
     * Creates Ldap properties. Primarily used in tests.
     *
     * @param host LDAP server
     * @param port LDAP port
     * @param baseDn Base distinguished name
     * @param rdnIdentifier Relative distinguished name
     * @param prefix Prefix for paths that should require LDAP authentication
     * @param whitelistedPaths Paths that should be excluded from LDAP authentication (includes sub-paths)
     * @return Ldap properties
     */
    public static LdapProperties ldapProperties(final String host,
                                                final int port,
                                                final String baseDn,
                                                final String rdnIdentifier,
                                                final String prefix,
                                                final String... whitelistedPaths) {
        final LdapProperties ldap = new LdapProperties();
        ldap.setEnabled(true);
        ldap.setHost(host);
        ldap.setPort(port);
        ldap.setBaseDn(baseDn);
        ldap.setRdnIdentifier(rdnIdentifier);
        ldap.setPrefix(prefix);
        ldap.setWhitelistedPaths(Arrays.asList(whitelistedPaths));
        return ldap;
    }

    /**
     * Validate LdapProperties
     * @return true if properties are valid, false otherwise
     */
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getWhitelistedPaths() {
        return whitelistedPaths;
    }

    public void setWhitelistedPaths(List<String> whitelistedPaths) {
        this.whitelistedPaths = whitelistedPaths;
    }
}
