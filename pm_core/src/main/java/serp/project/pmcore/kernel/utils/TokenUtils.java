/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.kernel.property.KeycloakProperties;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class TokenUtils {
    private final HttpClientHelper httpClientHelper;
    private final KeycloakProperties keycloakProperties;

    public Optional<String> getServiceToken() {
        String tokenUrl = keycloakProperties.getUrl() + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        var formData = new org.springframework.util.LinkedMultiValueMap<String, String>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());

        try {
            var response = httpClientHelper
                    .post(tokenUrl, formData, Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                return Optional.empty();
            }
            return Optional.of(response.get("access_token").toString());

        } catch (Exception e) {
            log.error("Error obtaining service token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
