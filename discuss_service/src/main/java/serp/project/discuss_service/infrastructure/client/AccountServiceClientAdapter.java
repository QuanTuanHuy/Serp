/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Client to call Account Service APIs
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.project.discuss_service.kernel.utils.HttpClientHelper;
import serp.project.discuss_service.kernel.utils.TokenUtils;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClientAdapter implements IAccountServiceClient {

    private final HttpClientHelper httpClientHelper;
    private final TokenUtils tokenUtils;

    @Value("${services.account.url}")
    private String accountServiceUrl;

    @Override
    public Optional<ChannelMemberResponse.UserInfo> getUserById(Long userId) {
        try {
            String token = tokenUtils.getServiceToken()
                    .orElseThrow(() -> new RuntimeException("Failed to obtain service token"));

            String url = accountServiceUrl + "/internal/api/v1/users/" + userId;
            log.info("Url: {}", url);

            Map<String, String> headers = Map.of("Authorization", "Bearer " + token);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpClientHelper.get(url, null, headers, Map.class)
                    .block();

            if (response == null) {
                log.warn("No response received for user ID: {}", userId);
                return Optional.empty();
            }

            return extractUserInfo(response, userId);

        } catch (Exception e) {
            log.error("Error fetching user {} from account service: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract UserInfo from account service response.
     */
    @SuppressWarnings("unchecked")
    private Optional<ChannelMemberResponse.UserInfo> extractUserInfo(Map<String, Object> response, Long userId) {
        try {
            // Response structure: { code: 200, data: { id, name, email, avatar } }
            Object dataObj = response.get("data");
            if (dataObj == null) {
                log.warn("No data field in response for user ID: {}", userId);
                return Optional.empty();
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;

            String name = getStringValue(data, "name", "firstName", "lastName");
            String email = getStringValue(data, "email");
            String avatarUrl = getStringValue(data, "avatar", "avatarUrl", "profileImage");

            return Optional.of(ChannelMemberResponse.UserInfo.builder()
                    .id(userId)
                    .name(name)
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .build());

        } catch (Exception e) {
            log.error("Error parsing user info for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get string value from map, trying multiple possible keys.
     */
    private String getStringValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
}
