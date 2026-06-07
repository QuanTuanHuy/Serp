/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.kernel.property.ExternalServiceProperties;
import serp.project.crm.kernel.utils.HttpClientHelper;
import serp.project.crm.kernel.utils.JsonUtils;
import serp.project.crm.kernel.utils.TokenUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileClientAdapter implements IUserProfileClient {

    private final TokenUtils tokenUtils;
    private final HttpClientHelper httpClientHelper;
    private final ExternalServiceProperties serviceProperties;

    private final JsonUtils jsonUtils;

    @Override
    public UserProfileResponse getUserProfileById(Long userId) {
        try {
            String serviceToken = tokenUtils.getServiceToken();

            String url = serviceProperties.getServiceUrlByName(Constants.ServiceNames.ACCOUNT_SERVICE)
                    + "/account-service/internal/api/v1/users/" + userId;
            var response = httpClientHelper
                    .get(url, null, Map.of("Authorization", "Bearer " + serviceToken), GeneralResponse.class)
                    .block();
            if (response != null && response.isSuccess()) {
                return jsonUtils.fromJson(jsonUtils.toJson(response.getData()), UserProfileResponse.class);
            }

            return null;
        } catch (AppException e) {
            log.error("[UserProfileClientAdapter] error when get user profile: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserProfileClientAdapter] unexpected error when get user profile: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<UserProfileResponse> getUserProfilesByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String serviceToken = tokenUtils.getServiceToken();

            String url = serviceProperties.getServiceUrlByName(Constants.ServiceNames.ACCOUNT_SERVICE)
                    + "/account-service/internal/api/v1/users/batch";

            String idsParam = userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            var queryParams = new org.springframework.util.LinkedMultiValueMap<String, String>();
            queryParams.add("ids", idsParam);

            GeneralResponse<?> response = httpClientHelper
                    .get(url, queryParams, Map.of("Authorization", "Bearer " + serviceToken), GeneralResponse.class)
                    .block();

            if (response != null && response.isSuccess()) {
                List<?> dataList = (List<?>) response.getData();
                return dataList.stream()
                        .map(item -> jsonUtils.fromJson(jsonUtils.toJson(item), UserProfileResponse.class))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        } catch (AppException e) {
            log.error("[UserProfileClientAdapter] error when get user profiles batch: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserProfileClientAdapter] unexpected error when get user profiles batch: {}",
                    e.getMessage());
            throw e;
        }
    }

}
