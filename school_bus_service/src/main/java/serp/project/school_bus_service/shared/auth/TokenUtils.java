package serp.project.school_bus_service.shared.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.Optional;

/**
 * Utility to obtain Service-to-Service JWT access token from Keycloak
 * using Client Credentials Grant flow.
 */
@Component
@Slf4j
public class TokenUtils {

    private final RestClient restClient;

    @Value("${school-bus.keycloak.url:http://localhost:8180}")
    private String keycloakUrl;

    @Value("${school-bus.keycloak.realm:serp}")
    private String realm;

    @Value("${school-bus.keycloak.client-id:serp-school-bus}")
    private String clientId;

    @Value("${school-bus.keycloak.client-secret:your-client-secret}")
    private String clientSecret;

    public TokenUtils(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Call Keycloak Token Endpoint to obtain service access token.
     */
    public Optional<String> getServiceToken() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        log.debug("Fetching service token from URL: {}", tokenUrl);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                return Optional.of(response.get("access_token").toString());
            }
            log.warn("Keycloak token response did not contain 'access_token'");
            return Optional.empty();

        } catch (Exception e) {
            log.error("Failed to obtain service credentials token from Keycloak at URL: {}. Error: {}", tokenUrl, e.getMessage());
            return Optional.empty();
        }
    }
}
