package serp.project.pmcore.application.command.project.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CreateProjectValidator {

    private final IProjectPort projectPort;
    private final IProjectCategoryPort categoryPort;
    private final IProjectBlueprintPort blueprintPort;
    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IPrioritySchemePort prioritySchemePort;

    private static final Pattern KEY_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

    private static final Set<String> VALID_PROJECT_TYPES =
            Set.of("software", "business");

    private static final Set<String> VALID_ASSOCIATION_MODES =
            Set.of("SHARED_ASSOCIATION", "CLONE_ON_ASSOCIATE");


    public void validate(CreateProjectRequest request, Long tenantId) {
        validateProjectMetadata(request, tenantId);
        validateSchemeOverrides(request, tenantId);
        validateBlueprintConsistency(request, tenantId);
    }

    private void validateProjectMetadata(CreateProjectRequest request, Long tenantId) {
        validateKeyFormat(request.getKey());
        validateKeyUniqueness(request.getKey(), tenantId);
        validateProjectType(request.getProjectTypeKey());
        validateAssociationMode(request.getAssociationMode());
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

    private void validateAssociationMode(String associationMode) {
        if (associationMode == null) return;
        if (!VALID_ASSOCIATION_MODES.contains(associationMode.toUpperCase())) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_ASSOCIATION_MODE,
                    "Association mode must be one of " + VALID_ASSOCIATION_MODES
                            + ", got: '" + associationMode + "'");
        }
    }

    private void validateCategoryExists(Long categoryId, Long tenantId) {
        if (categoryId == null) return;
        categoryPort.getCategoryByIdIncludingSystem(categoryId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.category(categoryId));
    }

    private void validateSchemeOverrides(CreateProjectRequest request, Long tenantId) {
        validateIssueTypeSchemeExists(request.getIssueTypeSchemeId(), tenantId);
        validateWorkflowSchemeExists(request.getWorkflowSchemeId(), tenantId);
        validatePrioritySchemeExists(request.getPrioritySchemeId(), tenantId);
        validateFieldConfigSchemeExists(request.getFieldConfigSchemeId(), tenantId);
        validateIssueTypeScreenSchemeExists(request.getIssueTypeScreenSchemeId(), tenantId);
        validatePermissionSchemeExists(request.getPermissionSchemeId(), tenantId);
        validateNotificationSchemeExists(request.getNotificationSchemeId(), tenantId);
        validateIssueSecuritySchemeExists(request.getIssueSecuritySchemeId(), tenantId);
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
    }

    private void validateIssueTypeScreenSchemeExists(Long schemeId, Long tenantId) {
    }

    private void validatePermissionSchemeExists(Long schemeId, Long tenantId) {
    }

    private void validateNotificationSchemeExists(Long schemeId, Long tenantId) {
    }

    private void validateIssueSecuritySchemeExists(Long schemeId, Long tenantId) {
    }


    private void validateBlueprintConsistency(CreateProjectRequest request, Long tenantId) {
        if (request.getBlueprintId() == null) {
            validateMandatorySchemesWhenNoBlueprint(request, tenantId);
            return;
        }

        validateBlueprintExists(request.getBlueprintId(), tenantId);
        validateBlueprintProjectTypeMatch(request.getBlueprintId(),
                request.getProjectTypeKey(), tenantId);
        validateBlueprintProvidesRequiredSchemes(request, tenantId);
    }

    private void validateMandatorySchemesWhenNoBlueprint(CreateProjectRequest request,
                                                         Long tenantId) {
        List<String> missing = new ArrayList<>();

        if (request.getIssueTypeSchemeId() == null) missing.add("issueTypeSchemeId");
        if (request.getWorkflowSchemeId() == null) missing.add("workflowSchemeId");
        if (request.getPrioritySchemeId() == null) missing.add("prioritySchemeId");

        if (!missing.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing required scheme overrides when no blueprint is provided: "
                            + missing + ". Provide explicit scheme IDs or specify a blueprintId.");
        }
    }

    private void validateBlueprintExists(Long blueprintId, Long tenantId) {
        blueprintPort.getBlueprintByIdIncludingSystem(blueprintId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.blueprint(blueprintId));
    }

    private void validateBlueprintProjectTypeMatch(Long blueprintId,
                                                   String projectTypeKey,
                                                   Long tenantId) {
        blueprintPort.getBlueprintByIdIncludingSystem(blueprintId, tenantId)
                .filter(bp -> bp.getTypeKey() != null)
                .ifPresent(bp -> {
                    if (!bp.getTypeKey().equalsIgnoreCase(projectTypeKey)) {
                        throw new DomainValidationException(
                                DomainErrorCode.BLUEPRINT_PROJECT_TYPE_MISMATCH,
                                "Blueprint '" + bp.getName() + "' is for project type '"
                                        + bp.getTypeKey() + "', but request specifies '"
                                        + projectTypeKey + "'");
                    }
                });
    }

    private void validateBlueprintProvidesRequiredSchemes(CreateProjectRequest request,
                                                          Long tenantId) {
        Set<String> coveredByOverride = resolveOverriddenSchemeTypes(request);

        Set<String> coveredByBlueprint = blueprintSchemeDefaultPort
                .getDefaultsByBlueprintIdIncludingSystem(request.getBlueprintId(), tenantId)
                .stream()
                .map(d -> d.getSchemeType().toString())
                .collect(Collectors.toSet());

        List<String> missing = Stream.of("ISSUE_TYPE", "WORKFLOW", "PRIORITY")
                .filter(type -> !coveredByOverride.contains(type)
                        && !coveredByBlueprint.contains(type))
                .toList();

        if (!missing.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Blueprint does not provide defaults for required scheme types: "
                            + missing + ". Either add blueprint defaults or provide explicit "
                            + "scheme IDs in the request.");
        }
    }

    private Set<String> resolveOverriddenSchemeTypes(CreateProjectRequest request) {
        Set<String> covered = new HashSet<>();
        if (request.getIssueTypeSchemeId() != null) covered.add("ISSUE_TYPE");
        if (request.getWorkflowSchemeId() != null) covered.add("WORKFLOW");
        if (request.getPrioritySchemeId() != null) covered.add("PRIORITY");
        if (request.getFieldConfigSchemeId() != null) covered.add("FIELD_CONFIG");
        if (request.getIssueTypeScreenSchemeId() != null) covered.add("SCREEN");
        if (request.getPermissionSchemeId() != null) covered.add("PERMISSION");
        if (request.getNotificationSchemeId() != null) covered.add("NOTIFICATION");
        if (request.getIssueSecuritySchemeId() != null) covered.add("ISSUE_SECURITY");
        return covered;
    }
}