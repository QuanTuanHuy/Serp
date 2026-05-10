/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.enums.ExternalServices;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleActorSubjectValidator {

    private final IUserProfileClient userProfileClient;

    public ProjectRoleActorSubjectType validateAndNormalizeSubjectType(String rawSubjectType) {
        if (rawSubjectType == null || rawSubjectType.isBlank()) {
            throw new DomainValidationException(
                    DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID,
                    "subjectType is required"
            );
        }

        try {
            return ProjectRoleActorSubjectType.fromValue(rawSubjectType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException(
                    DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID,
                    "subjectType must be one of USER, GROUP, SERVICE_ACCOUNT"
            );
        }
    }

    public String validateAndNormalizeSubjectId(String rawSubjectId) {
        if (rawSubjectId == null || rawSubjectId.isBlank()) {
            log.error("subjectId is required: rawSubjectId={}", rawSubjectId);
            throw new DomainValidationException(
                    DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID,
                    "subjectId is required"
            );
        }

        String normalizedSubjectId = rawSubjectId.trim();
        if (normalizedSubjectId.length() > 255) {
            throw new DomainValidationException(
                    DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID,
                    "subjectId length must be <= 255"
            );
        }

        return normalizedSubjectId;
    }

    public void validateSubjectExistsForAdd(ProjectRoleActorSubjectType subjectType, String subjectId) {
        switch (subjectType) {
            case USER -> validateUserExists(subjectId);
            case SERVICE_ACCOUNT -> validateServiceAccountExists(subjectId);
            case GROUP -> {
                // Group existence endpoint is not available in Account service at the moment.
                // Keep strict format validation and defer remote existence check.
            }
        }
    }

    private void validateUserExists(String subjectId) {
        Long userId;
        try {
            userId = Long.parseLong(subjectId);
        } catch (NumberFormatException ex) {
            throw new DomainValidationException(
                    DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID,
                    "USER subjectId must be a numeric user id"
            );
        }

        log.info("Validating user existence: userId={}", userId);

        try {
            var profile = userProfileClient.getUserProfileById(userId);
            if (profile == null) {
                throw ResourceNotFoundException.user(userId);
            }
        } catch (AppException ex) {
            if (ex.getCode() == 404) {
                throw ResourceNotFoundException.user(userId);
            }
            throw ex;
        }
    }

    private void validateServiceAccountExists(String subjectId) {
        String normalizedClientId = subjectId.toLowerCase(Locale.ROOT);
        if (!ExternalServices.isValidClientId(normalizedClientId)) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.SERVICE_ACCOUNT_NOT_FOUND,
                    "Service account not found: clientId=" + subjectId
            );
        }
    }

}
