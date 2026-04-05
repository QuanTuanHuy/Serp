/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import serp.project.pmcore.application.workitem.command.create.internal.FieldRef;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.domain.screen.port.IScreenTabPort;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkItemFieldPolicyResolver {

    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IFieldConfigPort fieldConfigPort;
    private final IFieldConfigItemPort fieldConfigItemPort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final IScreenPort screenPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;

    public WorkItemFieldRules resolveCreateFieldRules(ProjectEntity project, Long issueTypeId, Long tenantId) {
        Long fieldConfigId = resolveFieldConfigId(project, issueTypeId, tenantId);
        Long createScreenId = resolveCreateScreenId(project, issueTypeId, tenantId);

        List<FieldConfigItemEntity> fieldConfigItems = fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(fieldConfigId, tenantId);
        List<FieldRef> createScreenFields = loadCreateScreenFields(createScreenId, tenantId);

        Map<String, WorkItemFieldPolicy> systemPolicies = new LinkedHashMap<>();
        Map<String, WorkItemFieldPolicy> customPolicies = new LinkedHashMap<>();

        for (FieldConfigItemEntity fieldConfigItem : fieldConfigItems) {
            FieldRef fieldRef = normalizeFieldRef(fieldConfigItem.getFieldRefType(), fieldConfigItem.getFieldRef());
            if (fieldRef == null) {
                continue;
            }
            mergeFieldPolicy(
                    fieldRef,
                    Boolean.TRUE.equals(fieldConfigItem.getIsRequired()),
                    Boolean.TRUE.equals(fieldConfigItem.getIsHidden()),
                    false,
                    systemPolicies,
                    customPolicies
            );
        }

        for (FieldRef createScreenField : createScreenFields) {
            mergeFieldPolicy(createScreenField, false, false, true, systemPolicies, customPolicies);
        }

        mergeFieldPolicy(
                new FieldRef(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM, WorkItemFieldConstants.SUMMARY),
                true,
                false,
                true,
                systemPolicies,
                customPolicies
        );
        mergeFieldPolicy(
                new FieldRef(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM, WorkItemFieldConstants.ISSUE_TYPE_ID),
                false,
                false,
                true,
                systemPolicies,
                customPolicies
        );

        return new WorkItemFieldRules(systemPolicies, customPolicies);
    }

    private Long resolveFieldConfigId(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getFieldConfigSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                    "Project has no field configuration scheme binding: projectId=" + project.getId()
            );
        }

        FieldConfigSchemeEntity fieldConfigScheme = fieldConfigSchemePort
                .getFieldConfigSchemeById(project.getFieldConfigSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found: id=" + project.getFieldConfigSchemeId()
                ));

        Long fieldConfigId = fieldConfigSchemeItemPort.getFieldConfigSchemeItemsBySchemeId(project.getFieldConfigSchemeId(), tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(FieldConfigSchemeItemEntity::getFieldConfigId)
                .findFirst()
                .orElse(fieldConfigScheme.getDefaultFieldConfigId());

        if (fieldConfigId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_COVERAGE_MISSING,
                    "Field configuration scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        fieldConfigPort.getFieldConfigById(fieldConfigId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_NOT_FOUND,
                        "Field configuration not found: id=" + fieldConfigId
                ));

        return fieldConfigId;
    }

    private Long resolveCreateScreenId(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getIssueTypeScreenSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                    "Project has no issue type screen scheme binding: projectId=" + project.getId()
            );
        }

        IssueTypeScreenSchemeEntity issueTypeScreenScheme = issueTypeScreenSchemePort
                .getIssueTypeScreenSchemeById(project.getIssueTypeScreenSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found: id=" + project.getIssueTypeScreenSchemeId()
                ));

        Long screenSchemeId = issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(project.getIssueTypeScreenSchemeId(), tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(IssueTypeScreenSchemeItemEntity::getScreenSchemeId)
                .findFirst()
                .orElse(issueTypeScreenScheme.getDefaultScreenSchemeId());

        if (screenSchemeId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_COVERAGE_MISSING,
                    "Issue type screen scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        ScreenSchemeEntity screenScheme = screenSchemePort.getScreenSchemeById(screenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found: id=" + screenSchemeId
                ));

        Long createScreenId = screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(screenSchemeId, tenantId)
                .stream()
                .filter(item -> WorkItemFieldConstants.CREATE_OPERATION_KEY.equalsIgnoreCase(item.getOperationKey()))
                .map(ScreenSchemeItemEntity::getScreenId)
                .findFirst()
                .orElse(screenScheme.getDefaultScreenId());

        if (createScreenId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCREEN_SCHEME_OPERATION_COVERAGE_MISSING,
                    "CREATE screen is not resolvable for screenSchemeId=" + screenSchemeId
            );
        }

        screenPort.getScreenById(createScreenId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_NOT_FOUND,
                        "CREATE screen not found: id=" + createScreenId
                ));

        return createScreenId;
    }

    private List<FieldRef> loadCreateScreenFields(Long createScreenId, Long tenantId) {
        List<ScreenTabEntity> screenTabs = screenTabPort.getScreenTabsByScreenId(createScreenId, tenantId);
        List<FieldRef> fieldRefs = new ArrayList<>();
        for (ScreenTabEntity screenTab : screenTabs) {
            List<ScreenTabFieldEntity> screenTabFields = screenTabFieldPort
                    .getScreenTabFieldsByScreenTabId(screenTab.getId(), tenantId);
            for (ScreenTabFieldEntity screenTabField : screenTabFields) {
                FieldRef fieldRef = normalizeFieldRef(screenTabField.getFieldRefType(), screenTabField.getFieldRef());
                if (fieldRef != null) {
                    fieldRefs.add(fieldRef);
                }
            }
        }
        return fieldRefs;
    }

    private FieldRef normalizeFieldRef(String fieldRefType, String fieldRef) {
        if (fieldRefType == null || fieldRef == null || fieldRef.isBlank()) {
            return null;
        }

        String normalizedType = normalizeToken(fieldRefType);
        if (WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new FieldRef(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM, canonicalizeSystemFieldRef(fieldRef));
        }

        if (WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new FieldRef(WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM, fieldRef.trim());
        }

        return null;
    }

    private String canonicalizeSystemFieldRef(String fieldRef) {
        String normalized = normalizeToken(fieldRef);
        return switch (normalized) {
            case "issue_type_id", "issue_type", "issuetype" -> WorkItemFieldConstants.ISSUE_TYPE_ID;
            case "summary" -> WorkItemFieldConstants.SUMMARY;
            case "description" -> WorkItemFieldConstants.DESCRIPTION;
            case "priority_id", "priority" -> WorkItemFieldConstants.PRIORITY_ID;
            case "assignee_id", "assignee" -> WorkItemFieldConstants.ASSIGNEE_ID;
            case "parent_id", "parent" -> WorkItemFieldConstants.PARENT_ID;
            case "due_date", "due" -> WorkItemFieldConstants.DUE_DATE;
            case "time_original_estimate", "original_estimate" -> WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE;
            case "security_level_id", "security_level", "security" -> WorkItemFieldConstants.SECURITY_LEVEL_ID;
            default -> normalized;
        };
    }

    private String normalizeToken(String value) {
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void mergeFieldPolicy(FieldRef fieldRef,
                                  boolean required,
                                  boolean hidden,
                                  boolean onCreateScreen,
                                  Map<String, WorkItemFieldPolicy> systemPolicies,
                                  Map<String, WorkItemFieldPolicy> customPolicies) {
        Map<String, WorkItemFieldPolicy> targetPolicies = WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM.equals(fieldRef.fieldRefType())
                ? systemPolicies
                : customPolicies;

        WorkItemFieldPolicy existingPolicy = targetPolicies.get(fieldRef.fieldRef());
        boolean mergedRequired = required;
        boolean mergedHidden = hidden;
        boolean mergedOnCreateScreen = onCreateScreen;

        if (existingPolicy != null) {
            mergedRequired = existingPolicy.required() || mergedRequired;
            mergedHidden = existingPolicy.hidden() || mergedHidden;
            mergedOnCreateScreen = existingPolicy.onScreen() || mergedOnCreateScreen;
        }

        if (WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM.equals(fieldRef.fieldRefType())
                && WorkItemFieldConstants.ALWAYS_WRITABLE_ON_CREATE_SYSTEM_FIELDS.contains(fieldRef.fieldRef())) {
            mergedHidden = false;
            mergedOnCreateScreen = true;
        }

        if (WorkItemFieldConstants.SUMMARY.equals(fieldRef.fieldRef())) {
            mergedRequired = true;
        }

        targetPolicies.put(fieldRef.fieldRef(), new WorkItemFieldPolicy(
                fieldRef.fieldRefType(),
                fieldRef.fieldRef(),
                mergedRequired,
                mergedHidden,
                mergedOnCreateScreen
        ));
    }
}
