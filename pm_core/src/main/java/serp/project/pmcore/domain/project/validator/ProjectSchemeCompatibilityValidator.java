/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.issyetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issyetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issyetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProjectSchemeCompatibilityValidator {

    private static final Set<String> REQUIRED_SCREEN_OPERATIONS = Set.of("CREATE", "EDIT", "VIEW");

    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IIssueTypePort issueTypePort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IPriorityPort priorityPort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IFieldConfigPort fieldConfigPort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final IScreenPort screenPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final WorkflowSchemeCompatibilityValidator workflowSchemeCompatibilityValidator;

    public void validate(ProjectEntity project, Long tenantId) {
        Set<Long> issueTypeIds = validateIssueTypeScheme(project.getIssueTypeSchemeId(), tenantId);
        validatePriorityScheme(project.getPrioritySchemeId(), tenantId);
        workflowSchemeCompatibilityValidator.validate(project.getWorkflowSchemeId(), issueTypeIds, tenantId);
        validateFieldConfigScheme(project.getFieldConfigSchemeId(), issueTypeIds, tenantId);
        validateIssueTypeScreenScheme(project.getIssueTypeScreenSchemeId(), issueTypeIds, tenantId);
        validateIssueSecurityScheme(project.getIssueSecuritySchemeId(), tenantId);
        validateCustomFieldContextResolution(project, issueTypeIds, tenantId);
    }

    private void validateCustomFieldContextResolution(ProjectEntity project, Set<Long> issueTypeIds, Long tenantId) {
        // Gate 7 from Module 00 depends on custom-field context infra, which is not yet
        // modeled in the current Java module. Leave this hook here so the remaining
        // context-resolution validator can be added without changing the orchestration flow.
    }

    private Set<Long> validateIssueTypeScheme(Long schemeId, Long tenantId) {
        IssueTypeSchemeEntity scheme = issueTypeSchemePort.getIssueTypeSchemeById(schemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                        "Issue type scheme not found in tenant scope: id=" + schemeId
                ));

        List<IssueTypeSchemeItemEntity> items = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(schemeId, tenantId);
        Set<Long> issueTypeIds = new HashSet<>();
        for (IssueTypeSchemeItemEntity item : items) {
            issueTypePort.getIssueTypeById(item.getIssueTypeId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                            "Issue type scheme references issue type outside tenant scope: issueTypeId=" + item.getIssueTypeId()
                    ));
            issueTypeIds.add(item.getIssueTypeId());
        }

        if (scheme.getDefaultIssueTypeId() != null && !issueTypeIds.contains(scheme.getDefaultIssueTypeId())) {
            throw new DomainValidationException(
                    DomainErrorCode.ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS,
                    "Default issue type must be included in issue type scheme items: schemeId=" + schemeId
            );
        }

        return issueTypeIds;
    }

    private void validatePriorityScheme(Long schemeId, Long tenantId) {
        PrioritySchemeEntity scheme = prioritySchemePort.getPrioritySchemeById(schemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found in tenant scope: id=" + schemeId
                ));

        List<PrioritySchemeItemEntity> items = prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(schemeId, tenantId);
        Set<Long> priorityIds = new HashSet<>();
        for (PrioritySchemeItemEntity item : items) {
            priorityPort.getPriorityById(item.getPriorityId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.PRIORITY_NOT_FOUND,
                            "Priority scheme references priority outside tenant scope: priorityId=" + item.getPriorityId()
                    ));
            priorityIds.add(item.getPriorityId());
        }

        if (scheme.getDefaultPriorityId() != null && !priorityIds.contains(scheme.getDefaultPriorityId())) {
            throw new DomainValidationException(
                    DomainErrorCode.PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS,
                    "Default priority must be included in priority scheme items: schemeId=" + schemeId
            );
        }
    }

    private void validateFieldConfigScheme(Long schemeId, Set<Long> issueTypeIds, Long tenantId) {
        FieldConfigSchemeEntity scheme = fieldConfigSchemePort.getFieldConfigSchemeById(schemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found in tenant scope: id=" + schemeId
                ));

        if (scheme.getDefaultFieldConfigId() != null) {
            fieldConfigPort.getFieldConfigById(scheme.getDefaultFieldConfigId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.FIELD_CONFIG_NOT_FOUND,
                            "Default field configuration not found in tenant scope: id=" + scheme.getDefaultFieldConfigId()
                    ));
        }

        List<FieldConfigSchemeItemEntity> items = fieldConfigSchemeItemPort
                .getFieldConfigSchemeItemsBySchemeIdIncludingSystem(schemeId, tenantId);
        Set<Long> coveredIssueTypes = new HashSet<>();
        for (FieldConfigSchemeItemEntity item : items) {
            issueTypePort.getIssueTypeById(item.getIssueTypeId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                            "Field configuration scheme references issue type outside tenant scope: issueTypeId=" + item.getIssueTypeId()
                    ));
            fieldConfigPort.getFieldConfigById(item.getFieldConfigId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.FIELD_CONFIG_NOT_FOUND,
                            "Field configuration scheme references field configuration outside tenant scope: fieldConfigId=" + item.getFieldConfigId()
                    ));
            coveredIssueTypes.add(item.getIssueTypeId());
        }

        for (Long issueTypeId : issueTypeIds) {
            if (!coveredIssueTypes.contains(issueTypeId) && scheme.getDefaultFieldConfigId() == null) {
                throw new DomainValidationException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_COVERAGE_MISSING,
                        "Field configuration scheme does not cover issue type id=" + issueTypeId
                );
            }
        }
    }

    private void validateIssueTypeScreenScheme(Long schemeId, Set<Long> issueTypeIds, Long tenantId) {
        IssueTypeScreenSchemeEntity scheme = issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(schemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found in tenant scope: id=" + schemeId
                ));

        if (scheme.getDefaultScreenSchemeId() != null) {
            validateScreenSchemeOperationCoverage(scheme.getDefaultScreenSchemeId(), tenantId);
        }

        List<IssueTypeScreenSchemeItemEntity> items = issueTypeScreenSchemeItemPort
                .getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(schemeId, tenantId);
        Map<Long, Long> issueTypeScreenSchemeMap = new HashMap<>();
        for (IssueTypeScreenSchemeItemEntity item : items) {
            issueTypePort.getIssueTypeById(item.getIssueTypeId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                            "Issue type screen scheme references issue type outside tenant scope: issueTypeId=" + item.getIssueTypeId()
                    ));
            validateScreenSchemeOperationCoverage(item.getScreenSchemeId(), tenantId);
            issueTypeScreenSchemeMap.put(item.getIssueTypeId(), item.getScreenSchemeId());
        }

        for (Long issueTypeId : issueTypeIds) {
            if (!issueTypeScreenSchemeMap.containsKey(issueTypeId) && scheme.getDefaultScreenSchemeId() == null) {
                throw new DomainValidationException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_COVERAGE_MISSING,
                        "Issue type screen scheme does not cover issue type id=" + issueTypeId
                );
            }
        }
    }

    private void validateScreenSchemeOperationCoverage(Long screenSchemeId, Long tenantId) {
        ScreenSchemeEntity screenScheme = screenSchemePort.getScreenSchemeById(screenSchemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found in tenant scope: id=" + screenSchemeId
                ));

        if (screenScheme.getDefaultScreenId() != null) {
            screenPort.getScreenById(screenScheme.getDefaultScreenId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.SCREEN_NOT_FOUND,
                            "Default screen not found in tenant scope: screenId=" + screenScheme.getDefaultScreenId()
                    ));
        }

        List<ScreenSchemeItemEntity> items = screenSchemeItemPort
                .getScreenSchemeItemsByScreenSchemeIdIncludingSystem(screenSchemeId, tenantId);
        Set<String> operationKeys = new HashSet<>();
        for (ScreenSchemeItemEntity item : items) {
            screenPort.getScreenById(item.getScreenId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.SCREEN_NOT_FOUND,
                            "Screen scheme references screen outside tenant scope: screenId=" + item.getScreenId()
                    ));
            operationKeys.add(item.getOperationKey());
        }

        if (!operationKeys.containsAll(REQUIRED_SCREEN_OPERATIONS)) {
            throw new DomainValidationException(
                    DomainErrorCode.SCREEN_SCHEME_OPERATION_COVERAGE_MISSING,
                    "Screen scheme must define CREATE, EDIT, and VIEW operations: screenSchemeId=" + screenSchemeId
            );
        }
    }

    private void validateIssueSecurityScheme(Long schemeId, Long tenantId) {
        IssueSecuritySchemeEntity scheme = issueSecuritySchemePort.getIssueSecuritySchemeById(schemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found in tenant scope: id=" + schemeId
                ));

        List<IssueSecurityLevelEntity> levels = issueSecurityLevelPort.getIssueSecurityLevelsBySchemeIdIncludingSystem(schemeId, tenantId);
        Set<Long> levelIds = new HashSet<>();
        for (IssueSecurityLevelEntity level : levels) {
            levelIds.add(level.getId());
        }

        if (scheme.getDefaultLevelId() != null && !levelIds.contains(scheme.getDefaultLevelId())) {
            throw new DomainValidationException(
                    DomainErrorCode.SECURITY_LEVEL_DEFAULT_REQUIRED,
                    "Default issue security level must belong to the same scheme: schemeId=" + schemeId
            );
        }
    }
}
