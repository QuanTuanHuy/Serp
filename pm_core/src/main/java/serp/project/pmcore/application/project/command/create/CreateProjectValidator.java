/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.blueprint.port.IProjectBlueprintPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.notification.port.INotificationSchemePort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemePort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.project.dto.ProjectSchemeBindings;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;

import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CreateProjectValidator {

    private static final Pattern KEY_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

    private static final Set<String> VALID_PROJECT_TYPES =
            Set.of("software", "business");

    private final IProjectReadPort projectReadPort;
    private final IProjectCategoryPort categoryPort;
    private final IProjectBlueprintPort projectBlueprintPort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IPermissionSchemePort permissionSchemePort;
    private final INotificationSchemePort notificationSchemePort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;

    public void validate(CreateProjectCommand command) {
        validateProjectMetadata(command);
        validateProvisioningMode(command.provisioningMode());
        validateBlueprint(command);
        validateSchemeBindings(command.toSchemeBindings(), command.tenantId());
    }

    private void validateProjectMetadata(CreateProjectCommand command) {
        validateKeyFormat(command.key());
        validateKeyUniqueness(command.key(), command.tenantId());
        validateProjectType(command.projectTypeKey());
        validateCategoryExists(command.categoryId(), command.tenantId());
    }

    private void validateKeyFormat(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new DomainValidationException(
                    DomainErrorCode.PROJECT_KEY_INVALID_FORMAT,
                    "Project key must be 2-10 uppercase alphanumeric characters "
                            + "starting with a letter, got: '" + key + "'");
        }
    }

    private void validateKeyUniqueness(String key, Long tenantId) {
        if (projectReadPort.existsByKeyAndTenantId(key, tenantId)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_KEY_ALREADY_EXISTS,
                    "Project key '" + key + "' is already taken in this tenant");
        }
    }

    private void validateProjectType(String projectTypeKey) {
        if (projectTypeKey == null || !VALID_PROJECT_TYPES.contains(projectTypeKey)) {
            throw new DomainValidationException(
                    DomainErrorCode.PROJECT_TYPE_INVALID,
                    "Project type must be one of " + VALID_PROJECT_TYPES
                            + ", got: '" + projectTypeKey + "'");
        }
    }

    private void validateCategoryExists(Long categoryId, Long tenantId) {
        if (categoryId == null) {
            return;
        }

        categoryPort.getCategoryByIdIncludingSystem(categoryId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.category(categoryId));
    }

    private void validateProvisioningMode(ProvisioningMode provisioningMode) {
        if (provisioningMode == null) {
            return;
        }

        if (ProvisioningMode.CLONE_FROM_SHARED.equals(provisioningMode)) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_PROVISIONING_MODE,
                    "Create project supports only TEMPLATE_DEFAULT or SHARED_FROM_EXISTING. "
                            + "CLONE_FROM_SHARED is reserved for a future rebinding flow.");
        }
    }

    private void validateBlueprint(CreateProjectCommand command) {
        if (command.blueprintId() == null) {
            return;
        }

        projectBlueprintPort.getBlueprintByIdIncludingSystem(command.blueprintId(), command.tenantId())
                .ifPresentOrElse(blueprint -> {
                    if (blueprint.getTypeKey() != null
                            && !blueprint.getTypeKey().equalsIgnoreCase(command.projectTypeKey())) {
                        throw new DomainValidationException(
                                DomainErrorCode.BLUEPRINT_PROJECT_TYPE_MISMATCH,
                                "Blueprint '" + blueprint.getName() + "' is for project type '"
                                        + blueprint.getTypeKey() + "', but command specifies '"
                                        + command.projectTypeKey() + "'"
                        );
                    }
                }, () -> {
                    throw ResourceNotFoundException.blueprint(command.blueprintId());
                });
    }

    private void validateSchemeBindings(ProjectSchemeBindings schemeBindings, Long tenantId) {
        validateIssueTypeSchemeExists(schemeBindings.getIssueTypeSchemeId(), tenantId);
        validateWorkflowSchemeExists(schemeBindings.getWorkflowSchemeId(), tenantId);
        validateFieldConfigSchemeExists(schemeBindings.getFieldConfigSchemeId(), tenantId);
        validateIssueTypeScreenSchemeExists(schemeBindings.getIssueTypeScreenSchemeId(), tenantId);
        validatePermissionSchemeExists(schemeBindings.getPermissionSchemeId(), tenantId);
        validateNotificationSchemeExists(schemeBindings.getNotificationSchemeId(), tenantId);
        validatePrioritySchemeExists(schemeBindings.getPrioritySchemeId(), tenantId);
        validateIssueSecuritySchemeExists(schemeBindings.getIssueSecuritySchemeId(), tenantId);
    }

    private void validateIssueTypeSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                        "Issue type scheme not found: id=" + schemeId));
    }

    private void validateWorkflowSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found: id=" + schemeId));
    }

    private void validatePrioritySchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        prioritySchemePort.getPrioritySchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + schemeId));
    }

    private void validateFieldConfigSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        fieldConfigSchemePort.getFieldConfigSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found: id=" + schemeId));
    }

    private void validateIssueTypeScreenSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        issueTypeScreenSchemePort.getIssueTypeScreenSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found: id=" + schemeId));
    }

    private void validatePermissionSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        permissionSchemePort.getPermissionSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                        "Permission scheme not found: id=" + schemeId));
    }

    private void validateNotificationSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        notificationSchemePort.getNotificationSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.NOTIFICATION_SCHEME_NOT_FOUND,
                        "Notification scheme not found: id=" + schemeId));
    }

    private void validateIssueSecuritySchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return;
        }

        issueSecuritySchemePort.getIssueSecuritySchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + schemeId));
    }
}
