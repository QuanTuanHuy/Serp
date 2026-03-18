/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.project.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.IProjectCategoryPort;
import serp.project.pmcore.domain.port.store.IProjectPort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.INotificationSchemePort;
import serp.project.pmcore.domain.port.store.IPermissionSchemePort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;

import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CreateProjectValidator {

    private final IProjectPort projectPort;
    private final IProjectCategoryPort categoryPort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IPermissionSchemePort permissionSchemePort;
    private final INotificationSchemePort notificationSchemePort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;

    private static final Pattern KEY_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

    private static final Set<String> VALID_PROJECT_TYPES =
            Set.of("software", "business");


    public void validate(CreateProjectRequest request, Long tenantId) {
        validateProjectMetadata(request, tenantId);
        ProjectSchemeBindings schemeBindings = ProjectSchemeBindings.fromRequest(request);
        validateExplicitSchemeBindings(schemeBindings);
        validateSchemeBindings(schemeBindings, tenantId);
    }

    private void validateProjectMetadata(CreateProjectRequest request, Long tenantId) {
        validateKeyFormat(request.getKey());
        validateKeyUniqueness(request.getKey(), tenantId);
        validateProjectType(request.getProjectTypeKey());
        validateCategoryExists(request.getCategoryId(), tenantId);
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
        if (projectPort.existsByKeyAndTenantId(key, tenantId)) {
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
        if (categoryId == null) return;
        categoryPort.getCategoryByIdIncludingSystem(categoryId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.category(categoryId));
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
        if (schemeId == null) return;
        issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                        "Issue type scheme not found: id=" + schemeId));
    }

    private void validateWorkflowSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found: id=" + schemeId));
    }

    private void validatePrioritySchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        prioritySchemePort.getPrioritySchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + schemeId));
    }

    private void validateFieldConfigSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        fieldConfigSchemePort.getFieldConfigSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found: id=" + schemeId));
    }

    private void validateIssueTypeScreenSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        issueTypeScreenSchemePort.getIssueTypeScreenSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found: id=" + schemeId));
    }

    private void validatePermissionSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        permissionSchemePort.getPermissionSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                        "Permission scheme not found: id=" + schemeId));
    }

    private void validateNotificationSchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        notificationSchemePort.getNotificationSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.NOTIFICATION_SCHEME_NOT_FOUND,
                        "Notification scheme not found: id=" + schemeId));
    }

    private void validateIssueSecuritySchemeExists(Long schemeId, Long tenantId) {
        if (schemeId == null) return;
        issueSecuritySchemePort.getIssueSecuritySchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + schemeId));
    }


    private void validateExplicitSchemeBindings(ProjectSchemeBindings schemeBindings) {
        var missing = schemeBindings.getMissingRequiredFields();
        if (!missing.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing required explicit scheme IDs for project creation: "
                            + missing + ". In round one, all effective scheme bindings must be provided explicitly.");
        }
    }
}
