/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.kernel.utils.HttpClientHelper;
import serp.project.pmcore.kernel.utils.TokenUtils;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectComponentLeadValidator {

    private final HttpClientHelper httpClientHelper;
    private final TokenUtils tokenUtils;

    @Value("${services.account.url:http://localhost:8081/account-service}")
    private String accountServiceUrl;

    public void validateLeadUserExists(Long userId) {
        if (userId == null) {
            return;
        }

        String token = tokenUtils.getServiceToken()
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.SERVICE_UNAVAILABLE,
                        "Failed to obtain service token for account lookup"
                ));

        String url = accountServiceUrl + "/internal/api/v1/users/" + userId;
        Map<String, String> headers = Map.of("Authorization", "Bearer " + token);

        try {
            AccountEnvelope response = httpClientHelper.get(url, null, headers, AccountEnvelope.class);
            if (response == null || response.data() == null) {
                throw ResourceNotFoundException.user(userId);
            }
        } catch (AppException ex) {
            if (ex.getCode() == 404) {
                throw ResourceNotFoundException.user(userId);
            }
            log.warn("Failed to validate project component lead user: userId={}, code={}", userId, ex.getCode());
            throw ex;
        }
    }

    record AccountEnvelope(Object data) {
    }
}
