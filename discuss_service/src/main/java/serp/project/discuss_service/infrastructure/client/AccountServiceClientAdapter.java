/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Client to call Account Service APIs
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.project.discuss_service.kernel.utils.HttpClientHelper;
import serp.project.discuss_service.kernel.utils.TokenUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

            UserProfileEnvelope response = httpClientHelper.get(
                    url,
                    null,
                    headers,
                    UserProfileEnvelope.class);
            if (response == null || response.data() == null) {
                log.warn("No response received for user ID: {}", userId);
                return Optional.empty();
            }
            return Optional.ofNullable(response.data().toUserInfo());

        } catch (Exception e) {
            log.error("Error fetching user {} from account service: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UserInfo> getUsersByIds(List<Long> userIds) {
        throw new UnsupportedOperationException("Unimplemented method 'getUsersByIds'");
    }

    @Override
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

            Map<String, Object> params = new HashMap<>();
            params.put("organizationId", tenantId);
            params.put("search", query);
            params.put("page", 0);
            params.put("pageSize", 50);
            MultiValueMap<String, String> queryParams = httpClientHelper.buildQueryParams(params);

            UsersPageEnvelope response = httpClientHelper.get(
                    url,
                    queryParams,
                    headers,
                    UsersPageEnvelope.class);

            if (response == null || response.data() == null || response.data().items() == null) {
                log.warn("No response or empty items received for tenant ID: {}", tenantId);
                return Collections.emptyList();
            }
            return response.data().items().stream()
                    .map(UserProfileResponse::toUserInfo)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching users for tenant {} from account service: {}", tenantId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private record UserProfileResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            String avatarUrl) {

        public UserInfo toUserInfo() {
            String name = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
            return UserInfo.builder()
                    .id(id)
                    .name(name.trim().isEmpty() ? null : name.trim())
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .build();
        }
    }

    private record UserProfileEnvelope(
            String status,
            Integer code,
            String message,
            UserProfileResponse data) {
    }

    private record UsersPageEnvelope(
            String status,
            Integer code,
            String message,
            PagedResponse data) {
    }

    private record PagedResponse(
            List<UserProfileResponse> items,
            int currentPage,
            long totalItems,
            int totalPages) {
    }
}
