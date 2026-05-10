/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleActorSubjectValidatorTest {

    @Mock
    private IUserProfileClient userProfileClient;

    private RoleActorSubjectValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RoleActorSubjectValidator(userProfileClient);
    }

    @Test
    void validateAndNormalizeSubjectTypeShouldNormalizeValue() {
        ProjectRoleActorSubjectType subjectType = validator.validateAndNormalizeSubjectType(" user ");
        assertEquals(ProjectRoleActorSubjectType.USER, subjectType);
    }

    @Test
    void validateAndNormalizeSubjectTypeShouldRejectInvalidType() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> validator.validateAndNormalizeSubjectType("invalid")
        );
        assertTrue(ex.getMessage().contains("subjectType"));
    }

    @Test
    void validateAndNormalizeSubjectIdShouldTrimValue() {
        String subjectId = validator.validateAndNormalizeSubjectId("  42  ");
        assertEquals("42", subjectId);
    }

    @Test
    void validateAndNormalizeSubjectIdShouldRejectBlank() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> validator.validateAndNormalizeSubjectId("   ")
        );
        assertTrue(ex.getMessage().contains("subjectId is required"));
    }

    @Test
    void validateSubjectExistsForAddShouldRejectNonNumericUserId() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> validator.validateSubjectExistsForAdd(ProjectRoleActorSubjectType.USER, "abc")
        );
        assertTrue(ex.getMessage().contains("numeric"));
    }

    @Test
    void validateSubjectExistsForAddShouldRejectMissingUser() {
        when(userProfileClient.getUserProfileById(eq(99L)))
                .thenThrow(new AppException(ErrorCode.NOT_FOUND));

        assertThrows(
                ResourceNotFoundException.class,
                () -> validator.validateSubjectExistsForAdd(ProjectRoleActorSubjectType.USER, "99")
        );
    }

    @Test
    void validateSubjectExistsForAddShouldAcceptKnownServiceAccount() {
        validator.validateSubjectExistsForAdd(ProjectRoleActorSubjectType.SERVICE_ACCOUNT, "serp-account");
    }

    @Test
    void validateSubjectExistsForAddShouldRejectUnknownServiceAccount() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> validator.validateSubjectExistsForAdd(ProjectRoleActorSubjectType.SERVICE_ACCOUNT, "unknown-client")
        );
    }
}
