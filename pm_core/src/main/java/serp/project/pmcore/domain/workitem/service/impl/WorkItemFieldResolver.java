/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigItemPort;
import serp.project.pmcore.domain.fieldconfig.service.IFieldConfigService;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.util.WorkItemFieldUtils;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRef;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemFieldResolver implements IWorkItemFieldResolver {

    private final IFieldConfigItemPort fieldConfigItemPort;

    private final IScreenService screenService;
    private final IFieldConfigService fieldConfigService;

    @Override
    public WorkItemFieldRules resolveFieldRules(Long projectId,
                                                Long fieldConfigSchemeId,
                                                Long issueTypeId,
                                                Long screenId,
                                                Long tenantId) {
        if (screenId == null) {
            return WorkItemFieldRules.empty();
        }

        Long fieldConfigId = fieldConfigService.resolveFieldConfigId(
                fieldConfigSchemeId,
                issueTypeId,
                tenantId
        );
        List<FieldConfigItemEntity> fieldConfigItems = fieldConfigItemPort
                .getFieldConfigItemsByFieldConfigId(fieldConfigId, tenantId);
        List<WorkItemFieldRef> screenFields = loadScreenFields(screenId, tenantId);

        Map<String, WorkItemFieldPolicy> systemPolicies = new HashMap<>();
        Map<String, WorkItemFieldPolicy> customPolicies = new HashMap<>();

        for (FieldConfigItemEntity fieldConfigItem : fieldConfigItems) {
            WorkItemFieldRef fieldRef = normalizeFieldRef(fieldConfigItem.getFieldRefType(), fieldConfigItem.getFieldRef());
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

        for (WorkItemFieldRef fieldRef : screenFields) {
            mergeFieldPolicy(
                    fieldRef,
                    false,
                    false,
                    true,
                    systemPolicies,
                    customPolicies
            );
        }

        log.info("[WorkItemFieldResolver] Resolved field policies for project={}, issueTypeId={}, screenId={}, tenantId={}: systemFields={}, customFields={}",
                projectId, issueTypeId, screenId, tenantId, systemPolicies.keySet(), customPolicies.keySet());

        return new WorkItemFieldRules(systemPolicies, customPolicies);
    }

    private List<WorkItemFieldRef> loadScreenFields(Long screenId, Long tenantId) {
        Set<WorkItemFieldRef> fieldRefs = new HashSet<>();
        List<ScreenTabFieldEntity> tabFields = screenService.getScreenTabFieldsByScreenId(screenId, tenantId);
        if (tabFields.isEmpty()) {
            return Collections.emptyList();
        }

        for (ScreenTabFieldEntity tabField : tabFields) {
            WorkItemFieldRef fieldRef = normalizeFieldRef(tabField.getFieldRefType(), tabField.getFieldRef());
            if (fieldRef != null) {
                fieldRefs.add(fieldRef);
            }
        }

        return new ArrayList<>(fieldRefs);
    }

    private WorkItemFieldRef normalizeFieldRef(String fieldRefType, String fieldRef) {
        if (fieldRefType == null || fieldRef == null || fieldRef.isBlank()) {
            return null;
        }

        String normalizedType = normalizeToken(fieldRefType);
        if (WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new WorkItemFieldRef(
                    WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                    canonicalizeSystemFieldRef(fieldRef)
            );
        }

        if (WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new WorkItemFieldRef(
                    WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM,
                    fieldRef.trim()
            );
        }

        return null;
    }

    private String canonicalizeSystemFieldRef(String fieldRef) {
        return WorkItemFieldUtils.normalizeFieldRef(fieldRef);
    }

    private String normalizeToken(String value) {
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void mergeFieldPolicy(WorkItemFieldRef fieldRef,
                                  boolean required,
                                  boolean hidden,
                                  boolean onScreen,
                                  Map<String, WorkItemFieldPolicy> systemPolicies,
                                  Map<String, WorkItemFieldPolicy> customPolicies) {
        Map<String, WorkItemFieldPolicy> targetPolicies =
                WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM.equals(fieldRef.fieldRefType())
                        ? systemPolicies
                        : customPolicies;

        WorkItemFieldPolicy existing = targetPolicies.get(fieldRef.fieldRef());
        boolean mergedRequired = required;
        boolean mergedHidden = hidden;
        boolean mergedOnScreen = onScreen;

        if (existing != null) {
            mergedRequired = existing.required() || mergedRequired;
            mergedHidden = existing.hidden() || mergedHidden;
            mergedOnScreen = existing.onScreen() || mergedOnScreen;
        }

        targetPolicies.put(fieldRef.fieldRef(), new WorkItemFieldPolicy(
                fieldRef.fieldRefType(),
                fieldRef.fieldRef(),
                mergedRequired,
                mergedHidden,
                mergedOnScreen
        ));
    }
}
