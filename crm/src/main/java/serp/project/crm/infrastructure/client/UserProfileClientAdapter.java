/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.client;

import java.util.Map;

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
                    + "/internal/api/v1/users/" + userId;
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

}
