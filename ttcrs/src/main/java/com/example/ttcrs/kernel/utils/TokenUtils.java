package com.example.ttcrs.kernel.utils;

import com.example.ttcrs.config.KeycloakConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenUtils {

    private final HttpClientHelper httpClientHelper;
    private final KeycloakConfig keycloakConfig;

    public String getServiceToken() {
        String tokenUrl = keycloakConfig.getUrl()
                + "/realms/" + keycloakConfig.getRealm()
                + "/protocol/openid-connect/token";

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", keycloakConfig.getClientId());
        form.add("client_secret", keycloakConfig.getClientSecret());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpClientHelper
                    .post(tokenUrl, form, Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                throw new RuntimeException("Failed to obtain service token from Keycloak");
            }
            return response.get("access_token").toString();
        } catch (Exception e) {
            log.error("Error obtaining service token: {}", e.getMessage());
            throw new RuntimeException("Service token acquisition failed", e);
        }
    }
}
