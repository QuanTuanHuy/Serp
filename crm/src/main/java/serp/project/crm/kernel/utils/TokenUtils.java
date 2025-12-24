package serp.project.crm.kernel.utils;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.kernel.property.KeycloakProperties;

@RequiredArgsConstructor
@Slf4j
@Component
public class TokenUtils {
    private final HttpClientHelper httpClientHelper;
    private final KeycloakProperties keycloakProperties;

    public String getServiceToken() {
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
                throw new AppException(ErrorMessage.INTERNAL_SERVER_ERROR);
            }
            return response.get("access_token").toString();

        } catch (Exception e) {
            log.error("Error obtaining service token: {}", e.getMessage());
            throw new AppException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
