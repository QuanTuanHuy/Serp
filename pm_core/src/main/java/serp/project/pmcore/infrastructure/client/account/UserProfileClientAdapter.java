/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.client.account;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.shared.dto.external.ExternalGeneralResponse;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;
import serp.project.pmcore.kernel.utils.HttpClientHelper;
import serp.project.pmcore.kernel.utils.JsonUtils;
import serp.project.pmcore.kernel.utils.TokenUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileClientAdapter implements IUserProfileClient {

    private final TokenUtils tokenUtils;
    private final HttpClientHelper httpClientHelper;
    private final JsonUtils jsonUtils;

    @Value("${services.account.url:http://localhost:8081/account-service}")
    private String accountServiceUrl;

    @Override
    public UserProfileDto getUserProfileById(Long userId) {
        try {
            String serviceToken = tokenUtils.getServiceToken()
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.SERVICE_UNAVAILABLE,
                            "Failed to obtain service token for account lookup"
                    ));

            String url = accountServiceUrl + "/internal/api/v1/users/" + userId;
            Map<String, String> headers = Map.of("Authorization", "Bearer " + serviceToken);

            ExternalGeneralResponse response = httpClientHelper.get(url, null, headers, ExternalGeneralResponse.class);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return jsonUtils.fromJson(jsonUtils.toJson(response.getData()), UserProfileDto.class);
            }
            return null;
        } catch (AppException e) {
            log.error("[UserProfileClientAdapter] error when get user profile: {}", e.getMessage());
            throw e;
        } catch (DomainValidationException e) {
            log.error("[UserProfileClientAdapter] error when get user profile: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserProfileClientAdapter] unexpected error when get user profile: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<UserProfileDto> getUserProfilesByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String serviceToken = tokenUtils.getServiceToken()
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.SERVICE_UNAVAILABLE,
                            "Failed to obtain service token for account lookup"
                    ));

            String url = accountServiceUrl + "/internal/api/v1/users/batch";
            LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            for (Long id : userIds) {
                queryParams.add("ids", String.valueOf(id));
            }

            Map<String, String> headers = Map.of("Authorization", "Bearer " + serviceToken);

            ExternalGeneralResponse response = httpClientHelper.get(url, queryParams, headers,
                    ExternalGeneralResponse.class);

            if (response != null && response.isSuccess() && response.getData() instanceof List<?> dataList) {
                return dataList.stream()
                        .map(item -> jsonUtils.fromJson(jsonUtils.toJson(item), UserProfileDto.class))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        } catch (AppException e) {
            log.error("[UserProfileClientAdapter] error when get user profiles batch: {}", e.getMessage());
            throw e;
        } catch (DomainValidationException e) {
            log.error("[UserProfileClientAdapter] error when get user profiles batch: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserProfileClientAdapter] unexpected error when get user profiles batch: {}",
                    e.getMessage());
            throw e;
        }
    }
}
