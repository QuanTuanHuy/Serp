/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.workitem.create;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.dto.workitem.create.CreateFieldRules;
import serp.project.pmcore.domain.dto.workitem.create.CustomFieldResolutionContext;
import serp.project.pmcore.domain.dto.workitem.create.FieldPolicy;
import serp.project.pmcore.domain.dto.workitem.create.ResolvedCustomFields;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.port.store.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.domain.service.workitem.create.handler.IWorkItemCustomFieldValueHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class WorkItemCustomFieldResolver {

    private final ICustomFieldPort customFieldPort;
    private final ICustomFieldContextPort customFieldContextPort;
    private final ICustomFieldOptionPort customFieldOptionPort;
    private final ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    private final List<IWorkItemCustomFieldValueHandler> customFieldValueHandlers;

    public WorkItemCustomFieldResolver(ICustomFieldPort customFieldPort,
                                       ICustomFieldContextPort customFieldContextPort,
                                       ICustomFieldOptionPort customFieldOptionPort,
                                       ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort,
                                       List<IWorkItemCustomFieldValueHandler> customFieldValueHandlers) {
        this.customFieldPort = customFieldPort;
        this.customFieldContextPort = customFieldContextPort;
        this.customFieldOptionPort = customFieldOptionPort;
        this.customFieldContextDefaultValuePort = customFieldContextDefaultValuePort;
        this.customFieldValueHandlers = customFieldValueHandlers;
    }

    public ResolvedCustomFields resolveCustomFields(Long projectId,
                                                    Long issueTypeId,
                                                    Map<String, Object> requestCustomFields,
                                                    CreateFieldRules createFieldRules,
                                                    Long tenantId) {
        if (createFieldRules.customPolicies().isEmpty()) {
            return ResolvedCustomFields.empty();
        }

        List<String> fieldKeys = new ArrayList<>(new LinkedHashSet<>(createFieldRules.customPolicies().keySet()));
        Map<String, CustomFieldEntity> customFieldByKey = toCustomFieldMap(
                customFieldPort.getCustomFieldsByFieldKeysIncludingSystem(fieldKeys, tenantId)
        );

        List<WorkItemCustomFieldValueEntity> resolvedValues = new ArrayList<>();
        List<String> missingRequiredFields = new ArrayList<>();
        Map<String, Object> providedCustomFields = requestCustomFields == null ? Map.of() : requestCustomFields;

        for (Map.Entry<String, FieldPolicy> entry : createFieldRules.customPolicies().entrySet()) {
            String fieldKey = entry.getKey();
            FieldPolicy fieldPolicy = entry.getValue();
            CustomFieldEntity customField = customFieldByKey.get(fieldKey);

            if (customField == null) {
                throw new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Custom field definition not found for field=" + fieldKey
                );
            }

            CustomFieldContextEntity context = resolveCustomFieldContext(customField, projectId, issueTypeId, tenantId);
            List<CustomFieldContextDefaultValueEntity> defaultValues = customFieldContextDefaultValuePort
                    .getCustomFieldContextDefaultValuesByContextId(context.getId(), tenantId);
            List<CustomFieldOptionEntity> options = customFieldOptionPort.getCustomFieldOptionsByContextId(context.getId(), tenantId);

            CustomFieldResolutionContext resolutionContext = new CustomFieldResolutionContext(
                    customField.getId(),
                    context.getId(),
                    fieldKey,
                    normalizeTypeKey(customField.getTypeKey()),
                    options
            );
            IWorkItemCustomFieldValueHandler handler = resolveHandler(resolutionContext.normalizedTypeKey());

            boolean hasProvidedValue = providedCustomFields.containsKey(fieldKey) && providedCustomFields.get(fieldKey) != null;
            List<WorkItemCustomFieldValueEntity> fieldValues = hasProvidedValue
                    ? handler.resolveProvided(resolutionContext, providedCustomFields.get(fieldKey))
                    : handler.resolveDefaults(resolutionContext, defaultValues);

            if (fieldValues.isEmpty()) {
                if (fieldPolicy.required()) {
                    missingRequiredFields.add(fieldKey);
                }
                continue;
            }

            resolvedValues.addAll(fieldValues);
        }

        return new ResolvedCustomFields(resolvedValues, missingRequiredFields);
    }

    private Map<String, CustomFieldEntity> toCustomFieldMap(List<CustomFieldEntity> customFields) {
        Map<String, CustomFieldEntity> customFieldMap = new LinkedHashMap<>();
        for (CustomFieldEntity customField : customFields) {
            CustomFieldEntity existing = customFieldMap.get(customField.getFieldKey());
            if (existing == null || (Long.valueOf(0L).equals(existing.getTenantId()) && !Long.valueOf(0L).equals(customField.getTenantId()))) {
                customFieldMap.put(customField.getFieldKey(), customField);
            }
        }
        return customFieldMap;
    }

    private CustomFieldContextEntity resolveCustomFieldContext(CustomFieldEntity customField,
                                                               Long projectId,
                                                               Long issueTypeId,
                                                               Long tenantId) {
        List<CustomFieldContextEntity> applicableContexts = customFieldContextPort
                .getApplicableCustomFieldContexts(customField.getId(), projectId, issueTypeId, tenantId);

        if (applicableContexts.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "No custom field context matches field=" + customField.getFieldKey()
                            + ", projectId=" + projectId + ", issueTypeId=" + issueTypeId
            );
        }

        Map<Integer, List<CustomFieldContextEntity>> contextsBySpecificity = new LinkedHashMap<>();
        for (CustomFieldContextEntity applicableContext : applicableContexts) {
            contextsBySpecificity.computeIfAbsent(customFieldContextSpecificity(applicableContext), ignored -> new ArrayList<>())
                    .add(applicableContext);
        }

        Integer bestSpecificity = contextsBySpecificity.keySet().stream().min(Integer::compareTo)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Unable to determine context specificity for field=" + customField.getFieldKey()
                ));

        List<CustomFieldContextEntity> bestContexts = contextsBySpecificity.get(bestSpecificity);
        if (bestContexts == null || bestContexts.size() != 1) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "Custom field context is ambiguous for field=" + customField.getFieldKey()
                            + ", projectId=" + projectId + ", issueTypeId=" + issueTypeId
            );
        }

        return bestContexts.getFirst();
    }

    private int customFieldContextSpecificity(CustomFieldContextEntity context) {
        boolean allProjects = Boolean.TRUE.equals(context.getAppliesToAllProjects());
        boolean allIssueTypes = Boolean.TRUE.equals(context.getAppliesToAllIssueTypes());

        if (!allProjects && !allIssueTypes) {
            return 1;
        }
        if (!allProjects) {
            return 2;
        }
        if (!allIssueTypes) {
            return 3;
        }
        return 4;
    }

    private String normalizeTypeKey(String value) {
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private IWorkItemCustomFieldValueHandler resolveHandler(String normalizedTypeKey) {
        for (IWorkItemCustomFieldValueHandler handler : customFieldValueHandlers) {
            if (!handler.isFallback() && handler.supports(normalizedTypeKey)) {
                return handler;
            }
        }

        return customFieldValueHandlers.stream()
                .filter(IWorkItemCustomFieldValueHandler::isFallback)
                .findFirst()
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                        "No custom field handler available for type=" + normalizedTypeKey
                ));
    }
}
