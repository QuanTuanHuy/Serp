/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.service.IKeycloakService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUseCase {

    private final IKeycloakService keycloakService;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> getClientSecret(String clientId) {
        try {
            String clientSecret = keycloakService.getClientSecret(clientId);
            return responseUtils.success(clientSecret);
        } catch (Exception e) {
            log.error("Error in getClientSecret: {}", e.getMessage(), e);
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
