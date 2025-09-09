/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.response.TokenResponse;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ITokenService;
import serp.project.account.kernel.property.KeycloakProperties;
import serp.project.account.kernel.utils.HttpClientHelper;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService implements ITokenService {

    private final KeycloakProperties keycloakProperties;
    private final HttpClientHelper httpClientHelper;

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

        try {
            var response = httpClientHelper
                    .post(tokenUrl, formData, Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (response == null) {
                throw new AppException(Constants.ErrorMessage.WRONG_EMAIL_OR_PASSWORD);
            }

            return TokenResponse.builder()
                    .accessToken(response.get("access_token").toString())
                    .refreshToken(response.get("refresh_token").toString())
                    .expiresIn(Long.parseLong(response.get("expires_in").toString()))
                    .refreshExpiresIn(Long.parseLong(response.get("refresh_expires_in").toString()))
                    .tokenType(response.get("token_type").toString())
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while getting token for user: {}", username);
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR,
                    Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        String tokenUrl = keycloakProperties.getUrl() + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("refresh_token", refreshToken);

        try {
            var response = httpClientHelper
                    .post(tokenUrl, formData, Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (response == null) {
                throw new AppException(Constants.ErrorMessage.INVALID_REFRESH_TOKEN,
                        Constants.HttpStatusCode.UNAUTHORIZED);
            }

            return TokenResponse.builder()
                    .accessToken(response.get("access_token").toString())
                    .refreshToken(response.get("refresh_token").toString())
                    .expiresIn(Long.parseLong(response.get("expires_in").toString()))
                    .refreshExpiresIn(Long.parseLong(response.get("refresh_expires_in").toString()))
                    .tokenType(response.get("token_type").toString())
                    .build();

        } catch (AppException e) {
            throw new AppException(e.getMessage(), Constants.HttpStatusCode.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Unexpected error while refreshing token");
            throw new AppException(Constants.ErrorMessage.INVALID_REFRESH_TOKEN,
                    Constants.HttpStatusCode.UNAUTHORIZED);
        }
    }

    @Override
    public void revokeToken(String refreshToken) {
        String revokeUrl = keycloakProperties.getUrl() + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/revoke";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("token", refreshToken);
        formData.add("token_type_hint", "refresh_token");

        try {
            httpClientHelper
                    .post(revokeUrl, formData, Void.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            log.info("Token revoked successfully");

        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
