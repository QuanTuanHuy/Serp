package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.response.TokenResponse;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ITokenService;
import serp.project.account.kernel.property.KeycloakProperties;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService implements ITokenService {
    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    @Override
    public TokenResponse getUserToken(String username, String password) {
        String tokenUrl = keycloakProperties.getUrl() + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("username", username);
        formData.add("password", password);

        var response = webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (response == null || !response.containsKey("access_token") || !response.containsKey("refresh_token")) {
            throw new AppException(Constants.ErrorMessage.WRONG_EMAIL_OR_PASSWORD);
        }

        return TokenResponse.builder()
                .accessToken(response.get("access_token").toString())
                .refreshToken(response.get("refresh_token").toString())
                .expiresIn(Long.parseLong(response.get("expires_in").toString()))
                .refreshExpiresIn(Long.parseLong(response.get("refresh_expires_in").toString()))
                .tokenType(response.get("token_type").toString())
                .build();
    }
}
