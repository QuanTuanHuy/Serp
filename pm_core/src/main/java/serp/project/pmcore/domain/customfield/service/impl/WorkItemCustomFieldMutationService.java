/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.customfield.dto.ExistingCustomFieldValueState;
import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.dto.WorkItemCustomFieldMutationPlan;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldMutationService;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldResolver;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkItemCustomFieldMutationService implements IWorkItemCustomFieldMutationService {

    private final ICustomFieldPort customFieldPort;
    private final IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    private final IWorkItemCustomFieldResolver workItemCustomFieldResolver;

    @Override
    public WorkItemCustomFieldMutationPlan planCreate(String issueTypeKey,
                                                      Map<String, Object> requestCustomFields,
                                                      Map<String, Boolean> requiredByFieldKey) {
        ResolvedCustomFields resolvedCustomFields = workItemCustomFieldResolver.resolveCustomFields(
                issueTypeKey,
                safeRequestCustomFields(requestCustomFields),
                safeRequiredByFieldKey(requiredByFieldKey)
        );

        return new WorkItemCustomFieldMutationPlan(
                resolvedCustomFields.values(),
                resolvedCustomFields.missingFields(),
                List.of(),
                requestedFieldKeys(requestCustomFields)
        );
    }

    @Override
    public WorkItemCustomFieldMutationPlan planUpdate(String issueTypeKey,
                                                      Long workItemId,
                                                      Long tenantId,
                                                      Map<String, Object> requestCustomFields,
                                                      Map<String, Boolean> requiredByFieldKey) {
        Map<String, Object> requestedFields = safeRequestCustomFields(requestCustomFields);
        Map<String, Boolean> requiredFields = safeRequiredByFieldKey(requiredByFieldKey);
        if (requestedFields.isEmpty() && requiredFields.isEmpty()) {
            return WorkItemCustomFieldMutationPlan.empty();
        }

        Set<String> relevantFieldKeys = collectRelevantFieldKeys(requiredFields, requestedFields);
        Map<String, CustomFieldEntity> customFieldsByKey = loadCustomFieldsByKey(relevantFieldKeys);
        List<WorkItemCustomFieldValueEntity> existingValues = workItemId == null || tenantId == null
                ? List.of()
                : workItemCustomFieldValuePort.getActiveValuesByWorkItemId(workItemId, tenantId);
        Map<String, ExistingCustomFieldValueState> existingValuesByKey = mapExistingValuesByKey(customFieldsByKey, existingValues);

        Map<String, Object> nonNullRequestedFields = requestedFields.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        ResolvedCustomFields resolvedCustomFields = nonNullRequestedFields.isEmpty()
                ? ResolvedCustomFields.empty()
                : workItemCustomFieldResolver.resolveCustomFields(
                        issueTypeKey,
                        nonNullRequestedFields,
                        toRequestedRequiredMap(nonNullRequestedFields.keySet(), requiredFields)
                );

        Set<String> effectiveFieldKeys = new LinkedHashSet<>(existingValuesByKey.keySet());
        effectiveFieldKeys.removeAll(requestedFields.keySet());
        effectiveFieldKeys.addAll(toResolvedFieldKeys(customFieldsByKey, resolvedCustomFields.values()));

        List<String> missingRequiredFields = requiredFields.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .filter(fieldKey -> !effectiveFieldKeys.contains(fieldKey))
                .toList();

        List<Long> customFieldIdsToReplace = requestedFields.keySet().stream()
                .map(customFieldsByKey::get)
                .filter(Objects::nonNull)
                .map(CustomFieldEntity::getId)
                .distinct()
                .toList();

        return new WorkItemCustomFieldMutationPlan(
                resolvedCustomFields.values(),
                missingRequiredFields,
                customFieldIdsToReplace,
                requestedFields.keySet().stream().sorted().toList()
        );
    }

    @Override
    public void applyPlan(Long workItemId,
                          Long tenantId,
                          Long userId,
                          WorkItemCustomFieldMutationPlan plan) {
        if (plan == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (!plan.customFieldIdsToReplace().isEmpty()) {
            workItemCustomFieldValuePort.softDeleteByWorkItemIdAndCustomFieldIds(
                    workItemId,
                    plan.customFieldIdsToReplace(),
                    userId,
                    now
            );
        }

        if (plan.resolvedValues().isEmpty()) {
            return;
        }

        for (WorkItemCustomFieldValueEntity value : plan.resolvedValues()) {
            value.setWorkItemId(workItemId);
            value.setTenantId(tenantId);
            value.applyCreate(userId, now);
        }
        workItemCustomFieldValuePort.saveAll(plan.resolvedValues());
    }

    private Map<String, Object> safeRequestCustomFields(Map<String, Object> requestCustomFields) {
        return requestCustomFields == null ? Map.of() : requestCustomFields;
    }

    private Map<String, Boolean> safeRequiredByFieldKey(Map<String, Boolean> requiredByFieldKey) {
        return requiredByFieldKey == null ? Map.of() : requiredByFieldKey;
    }

    private List<String> requestedFieldKeys(Map<String, Object> requestCustomFields) {
        return safeRequestCustomFields(requestCustomFields).keySet().stream().sorted().toList();
    }

    private Set<String> collectRelevantFieldKeys(Map<String, Boolean> requiredFields,
                                                 Map<String, Object> requestedFields) {
        LinkedHashSet<String> fieldKeys = new LinkedHashSet<>(requiredFields.keySet());
        fieldKeys.addAll(requestedFields.keySet());
        return fieldKeys;
    }

    private Map<String, CustomFieldEntity> loadCustomFieldsByKey(Collection<String> fieldKeys) {
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return Map.of();
        }

        return customFieldPort.getCustomFieldsByFieldKeys(new ArrayList<>(new LinkedHashSet<>(fieldKeys)))
                .stream()
                .collect(Collectors.toMap(
                        CustomFieldEntity::getFieldKey,
                        field -> field,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<String, ExistingCustomFieldValueState> mapExistingValuesByKey(Map<String, CustomFieldEntity> customFieldsByKey,
                                                                              List<WorkItemCustomFieldValueEntity> existingValues) {
        if (customFieldsByKey.isEmpty() || existingValues == null || existingValues.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> fieldKeyByCustomFieldId = customFieldsByKey.values().stream()
                .collect(Collectors.toMap(
                        CustomFieldEntity::getId,
                        CustomFieldEntity::getFieldKey,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<String, ExistingCustomFieldValueState> result = new LinkedHashMap<>();
        for (WorkItemCustomFieldValueEntity value : existingValues) {
            String fieldKey = fieldKeyByCustomFieldId.get(value.getCustomFieldId());
            if (fieldKey != null) {
                result.putIfAbsent(fieldKey, new ExistingCustomFieldValueState(value.getCustomFieldId(), fieldKey));
            }
        }
        return result;
    }

    private Map<String, Boolean> toRequestedRequiredMap(Set<String> requestedFieldKeys,
                                                        Map<String, Boolean> requiredFields) {
        return requestedFieldKeys.stream()
                .collect(Collectors.toMap(
                        fieldKey -> fieldKey,
                        fieldKey -> Boolean.TRUE.equals(requiredFields.get(fieldKey)),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Set<String> toResolvedFieldKeys(Map<String, CustomFieldEntity> customFieldsByKey,
                                            List<WorkItemCustomFieldValueEntity> resolvedValues) {
        if (resolvedValues == null || resolvedValues.isEmpty()) {
            return Set.of();
        }

        Map<Long, String> fieldKeyByCustomFieldId = customFieldsByKey.values().stream()
                .collect(Collectors.toMap(
                        CustomFieldEntity::getId,
                        CustomFieldEntity::getFieldKey,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        LinkedHashSet<String> fieldKeys = new LinkedHashSet<>();
        for (WorkItemCustomFieldValueEntity value : resolvedValues) {
            String fieldKey = fieldKeyByCustomFieldId.get(value.getCustomFieldId());
            if (fieldKey != null) {
                fieldKeys.add(fieldKey);
            }
        }
        return fieldKeys;
    }
}
