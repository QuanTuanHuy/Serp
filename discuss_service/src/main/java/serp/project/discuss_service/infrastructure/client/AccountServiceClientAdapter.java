/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Client to call Account Service APIs
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.project.discuss_service.kernel.utils.HttpClientHelper;
import serp.project.discuss_service.kernel.utils.TokenUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.grpc.enabled", havingValue = "false", matchIfMissing = true)
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

    @Override
    @SuppressWarnings("unchecked")
    public List<UserInfo> getUsersForTenant(Long tenantId, String query) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID must not be null");
        }
        try {
            String token = tokenUtils.getServiceToken()
                    .orElseThrow(() -> new RuntimeException("Failed to obtain service token"));

            String url = accountServiceUrl + "/internal/api/v1/users";
            log.info("Url: {}", url);

            Map<String, String> headers = Map.of("Authorization", "Bearer " + token);
            
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("organizationId", tenantId);
            params.put("search", query);
            params.put("page", 0);
            params.put("pageSize", 50);

            MultiValueMap<String, String> queryParams = httpClientHelper.buildQueryParams(params);

            Map<String, Object> response = httpClientHelper.get(
                    url,
                    queryParams,
                    headers,
                    Map.class
            ).block();

            if (response == null) {
                log.warn("No response received for users of tenant ID: {}", tenantId);
                return Collections.emptyList();
            }

            Object dataObj = response.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                Object itemsObj = dataMap.get("items");
                if (itemsObj instanceof List) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
                    return items.stream()
                            .map(this::mapToUserInfo)
                            .toList();
                }
            }

            log.warn("Invalid response structure for users of tenant ID: {}", tenantId);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching users for tenant {} from account service: {}", tenantId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extract UserInfo from account service response.
     */
    @SuppressWarnings("unchecked")
    private Optional<ChannelMemberResponse.UserInfo> extractUserInfo(Map<String, Object> response, Long userId) {
        try {
            Object dataObj = response.get("data");
            if (dataObj == null) {
                log.warn("No data field in response for user ID: {}", userId);
                return Optional.empty();
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;
            UserInfo userInfo = mapToUserInfo(data);
            if (userInfo.getId() == null) {
                userInfo.setId(userId);
            }

            return Optional.of(userInfo);

        } catch (Exception e) {
            log.error("Error parsing user info for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    private ChannelMemberResponse.UserInfo mapToUserInfo(Map<String, Object> data) {
        Long id = null;
        Object idObj = data.get("id");
        if (idObj instanceof Number) {
            id = ((Number) idObj).longValue();
        } else if (idObj != null) {
            try {
                id = Long.parseLong(idObj.toString());
            } catch (Exception e) {
                // ignore
            }
        }

        String name = getStringValue(data, "name", "firstName", "lastName");
        String email = getStringValue(data, "email");
        String avatarUrl = getStringValue(data, "avatar", "avatarUrl", "profileImage");

        return ChannelMemberResponse.UserInfo.builder()
                .id(id)
                .name(name)
                .email(email)
                .avatarUrl(avatarUrl)
                .build();
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
