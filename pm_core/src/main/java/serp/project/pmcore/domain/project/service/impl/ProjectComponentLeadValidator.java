/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectComponentLeadValidator {

    private final IUserProfileClient userProfileClient;

    public void validateLeadUserExists(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            var profile = userProfileClient.getUserProfileById(userId);
            if (profile == null) {
                log.error("User profile not found: userId={}", userId);
                throw ResourceNotFoundException.user(userId);
            }
        } catch (AppException ex) {
            if (ex.getCode() == 404) {
                log.error("User profile not found: userId={}", userId);
                throw ResourceNotFoundException.user(userId);
            }
            log.warn("Failed to validate project component lead user: userId={}, code={}", userId, ex.getCode());
            throw ex;
        }
    }
}
