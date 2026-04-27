/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UpdateProjectValidator {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

    private final IProjectReadPort projectReadPort;
    private final IProjectCategoryPort projectCategoryPort;
    private final RoleActorSubjectValidator roleActorSubjectValidator;

    public void validate(UpdateProjectCommand command, ProjectEntity existingProject) {
        validateAtLeastOneMutableField(command.data());
        validateName(command.data());
        validateKey(command.data(), existingProject, command.tenantId());
        validateLeadUser(command.data(), existingProject);
        validateCategory(command.data(), existingProject, command.tenantId());
    }

    private void validateAtLeastOneMutableField(ProjectUpdateData data) {
        if (!data.nameProvided()
                && !data.keyProvided()
                && !data.descriptionProvided()
                && !data.leadUserIdProvided()
                && !data.categoryIdProvided()
                && !data.urlProvided()
                && !data.avatarIdProvided()) {
            throw new IllegalArgumentException("At least one mutable project field must be provided");
        }
    }

    private void validateName(ProjectUpdateData data) {
        if (!data.nameProvided()) {
            return;
        }

        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name must not be blank when provided");
        }
    }

    private void validateKey(ProjectUpdateData data, ProjectEntity existingProject, Long tenantId) {
        if (!data.keyProvided()) {
            return;
        }

        if (data.key() == null || !KEY_PATTERN.matcher(data.key()).matches()) {
            throw new DomainValidationException(
                    DomainErrorCode.PROJECT_KEY_INVALID_FORMAT,
                    "Project key must be 2-10 uppercase alphanumeric characters starting with a letter"
            );
        }

        if (!data.key().equals(existingProject.getKey())
                && projectReadPort.existsByKeyAndTenantId(data.key(), tenantId)) {
            throw new serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_KEY_ALREADY_EXISTS,
                    "Project key '" + data.key() + "' is already taken in this tenant"
            );
        }
    }

    private void validateLeadUser(ProjectUpdateData data, ProjectEntity existingProject) {
        if (!data.leadUserIdProvided()) {
            return;
        }

        if (data.leadUserId() == null) {
            throw new IllegalArgumentException("Project lead must not be null when provided");
        }

        if (data.leadUserId().equals(existingProject.getLeadUserId())) {
            return;
        }

        roleActorSubjectValidator.validateSubjectExistsForAdd(
                ProjectRoleActorSubjectType.USER,
                String.valueOf(data.leadUserId())
        );
    }

    private void validateCategory(ProjectUpdateData data, ProjectEntity existingProject, Long tenantId) {
        if (!data.categoryIdProvided()) {
            return;
        }

        if (data.categoryId() == null || data.categoryId().equals(existingProject.getCategoryId())) {
            return;
        }

        projectCategoryPort.getCategoryByIdIncludingSystem(data.categoryId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.category(data.categoryId()));
    }
}
