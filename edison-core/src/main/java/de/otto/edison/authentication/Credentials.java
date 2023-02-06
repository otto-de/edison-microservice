package de.otto.edison.authentication;

import org.springframework.util.Base64Utils;
import org.springframework.util.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * LDAP credentials (username, password) parsed from HTTP request
 */
public class Credentials {

    private final String username;
    private final String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Read username and password from the request's {@code Authorization} header and create a {@code Credentials}
     * object. Requires authorization header to be base64 encoded.
     *
     * @param request incoming http request
     * @return {@code Optional} with parsed {@code Credentials} if {@code Authorization} header and credentials
     * are present, {@code Optional.empty} otherwise.
     */
    public static Optional<Credentials> readFrom(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (!ObjectUtils.isEmpty(authorizationHeader) && authorizationHeader.contains("Basic")) {
            String credentials = authorizationHeader.substring(6, authorizationHeader.length());
            Optional<String> decodedCredentials = base64Decode(credentials);
            String[] decodedCredentialParts = decodedCredentials
                    .map(s1 -> s1.split(":", 2))
                    .orElse(new String[0]);
            if (decodedCredentialParts.length >= 2
                    && !decodedCredentialParts[0].isEmpty()
                    && !decodedCredentialParts[1].isEmpty()) {

                return Optional.of(new Credentials(decodedCredentialParts[0], decodedCredentialParts[1]));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> base64Decode(String input) {
        try {
            return Optional.of(new String(Base64Utils.decode(input.getBytes())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
