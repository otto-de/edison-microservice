package de.otto.edison.authentication.configuration;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Properties used to configure LDAP authentication.
 *
 * Add an authentication filter to the web application context if edison.ldap property is set to {@code enabled}'.
 * All routes starting with the value of the {@code edison.ldap.prefix} property will be secured by LDAP. If no
 * property is set this will default to all routes starting with '/internal'.
 */
@ConfigurationProperties(prefix = "edison.ldap")
@Validated
public class LdapProperties {

    private static final Logger LOG = getLogger(LdapProperties.class);

    /**
     * Default paths that are whitelisted in any case.
     *
     * Note: /internal/js/ is the path for the JavaScript Code of edison. Has to be whitelisted to be excluded from authentication process.
     */
    private static final Collection<String> DEFAULT_WHITELIST = asList("/internal/js/", "/internal/health");

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
     * Base distinguished name (base DN).
     * If more than one is given, all will be tried to authenticate again LDAP
     */
    @NotEmpty
    private List<String> baseDn;

    /**
     * Distinguished name used to select user roles
     */
    private String roleBaseDn = null;

    /**
     * The role that is required to access LDAP secured paths. The available roles for an user are retrieved by
     * querying the LDAP tree using the {@link #roleBaseDn}. No sub tree branches are evaluated: The role has to
     * be directly located under the baseRoleDn.
     */
    private String requiredRole = null;

    /**
     * Relative distinguished name (RDN)
     */
    @NotEmpty
    private String rdnIdentifier;

    /**
     * * Prefix for LDAP secured paths, defaults to "/internal"
     *
     * @deprecated use {@link #prefixes};
     *             For backwards compatibility this deprecated prefix is automatically appended to {@link #prefixes}.
     */
    @Deprecated
    private String prefix = "/internal";

    /**
     * Prefix for LDAP secured paths, defaults to "/internal"
     */
    private Collection<String> prefixes = Collections.emptyList();

    /**
     * List of paths that should be whitelisted from LDAP authentication (sub-paths will also be whitelisted)
     */
    private Collection<String> whitelistedPaths = emptyList();

    /**
     * You can choose between StartTLS and SSL encryption for the LDAP server connection
     */
    private EncryptionType encryptionType = EncryptionType.StartTLS;



    /**
     * Creates Ldap properties. Primarily used in tests.
     *
     * @param host LDAP server
     * @param port LDAP port
     * @param baseDn Base distinguished name
     * @param roleBaseDn Base distinguished name used to select user roles
     * @param rdnIdentifier Relative distinguished name
     * @param prefix Prefix for paths that should require LDAP authentication
     * @param encryptionType StartTLS or SSL for the connection to the LDAP server
     * @param whitelistedPaths Paths that should be excluded from LDAP authentication (includes sub-paths)
     * @return Ldap properties
     */
    public static LdapProperties ldapProperties(final String host,
                                                final int port,
                                                final List<String> baseDn,
                                                final String roleBaseDn,
                                                final String rdnIdentifier,
                                                final String prefix,
                                                final EncryptionType encryptionType,
                                                final String... whitelistedPaths) {
        final LdapProperties ldap = new LdapProperties();
        ldap.setEnabled(true);
        ldap.setHost(host);
        ldap.setPort(port);
        ldap.setBaseDn(baseDn);
        ldap.setRoleBaseDn(roleBaseDn);
        ldap.setRdnIdentifier(rdnIdentifier);
        ldap.setPrefixes(singletonList(prefix));
        ldap.setEncryptionType(encryptionType);
        ldap.setWhitelistedPaths(asList(whitelistedPaths));
        return ldap;
    }

    /**
     * Validate LdapProperties
     * @return true if properties are valid, false otherwise
     */
    public boolean isValid() {
        if (isEmpty(host)) {
            LOG.error("host is undefined");
        } else if (baseDn == null || baseDn.isEmpty() || hasEmptyElements(baseDn)) {
            LOG.error("baseDn is undefined");
        } else if (isEmpty(rdnIdentifier)) {
            LOG.error("rdnIdentifier is undefined");
        } else {
            return true;
        }
        return false;
    }

    private boolean hasEmptyElements(List<String> listOfStrings) {
        List<String> listCopy =  new ArrayList<>(listOfStrings);
        return listCopy.removeAll(asList("", null));
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

    public List<String> getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(List<String> baseDn) {
        this.baseDn = baseDn;
    }

    public String getRoleBaseDn() {
        return roleBaseDn;
    }

    public void setRoleBaseDn(String roleBaseDn) {
        this.roleBaseDn = roleBaseDn;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public String getRdnIdentifier() {
        return rdnIdentifier;
    }

    public void setRdnIdentifier(String rdnIdentifier) {
        this.rdnIdentifier = rdnIdentifier;
    }

    @Deprecated
    public String getPrefix() {
        return prefix;
    }

    @Deprecated
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Collection<String> getPrefixes() {
        Collection<String> copy = new HashSet<>(prefixes);
        copy.add(prefix);
        return copy;
    }

    public void setPrefixes(Collection<String> prefixes) {
        this.prefixes = prefixes;
    }

    public Collection<String> getWhitelistedPaths() {
        Collection<String> copy = new HashSet<>(whitelistedPaths);
        copy.addAll(DEFAULT_WHITELIST);
        return whitelistedPaths;
    }

    public void setWhitelistedPaths(Collection<String> whitelistedPaths) {
        this.whitelistedPaths = whitelistedPaths;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

}
