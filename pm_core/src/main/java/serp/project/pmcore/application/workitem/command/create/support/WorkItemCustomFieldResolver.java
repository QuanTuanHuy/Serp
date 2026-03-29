/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.create.internal.CreateFieldRules;
import serp.project.pmcore.application.workitem.command.create.internal.CustomFieldResolutionContext;
import serp.project.pmcore.application.workitem.command.create.internal.FieldPolicy;
import serp.project.pmcore.application.workitem.command.create.internal.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldOptionPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.application.workitem.command.create.support.handler.IWorkItemCustomFieldValueHandler;

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

    public ResolvedCustomFields resolveCustomFields(String issueTypeKey,
                                                    Map<String, Object> requestCustomFields,
                                                    CreateFieldRules createFieldRules) {
        if (createFieldRules.customPolicies().isEmpty()) {
            return ResolvedCustomFields.empty();
        }

        List<String> fieldKeys = new ArrayList<>(new LinkedHashSet<>(createFieldRules.customPolicies().keySet()));
        Map<String, CustomFieldEntity> customFieldByKey = toCustomFieldMap(
                customFieldPort.getCustomFieldsByFieldKeys(fieldKeys)
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

            CustomFieldContextEntity context = resolveCustomFieldContext(customField, issueTypeKey);
            List<CustomFieldContextDefaultValueEntity> defaultValues = customFieldContextDefaultValuePort
                    .getCustomFieldContextDefaultValuesByContextId(context.getId());
            List<CustomFieldOptionEntity> options = customFieldOptionPort.getCustomFieldOptionsByContextId(context.getId());

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
            customFieldMap.putIfAbsent(customField.getFieldKey(), customField);
        }
        return customFieldMap;
    }

    private CustomFieldContextEntity resolveCustomFieldContext(CustomFieldEntity customField,
                                                               String issueTypeKey) {
        List<CustomFieldContextEntity> applicableContexts = customFieldContextPort
                .getApplicableCustomFieldContexts(customField.getId(), issueTypeKey);

        List<CustomFieldContextEntity> exactMatchContexts = applicableContexts.stream()
                .filter(context -> issueTypeKey.equals(context.getIssueTypeKey()))
                .toList();
        if (exactMatchContexts.size() == 1) {
            return exactMatchContexts.getFirst();
        }
        if (exactMatchContexts.size() > 1) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "Custom field context is ambiguous for field=" + customField.getFieldKey()
                            + ", issueTypeKey=" + issueTypeKey
            );
        }

        List<CustomFieldContextEntity> globalContexts = applicableContexts.stream()
                .filter(context -> context.getIssueTypeKey() == null)
                .toList();
        if (globalContexts.size() == 1) {
            return globalContexts.getFirst();
        }

        if (globalContexts.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "No custom field context matches field=" + customField.getFieldKey()
                            + ", issueTypeKey=" + issueTypeKey
            );
        }

        throw new DomainValidationException(
                DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                "Custom field context is ambiguous for field=" + customField.getFieldKey()
                        + ", issueTypeKey=" + issueTypeKey
        );
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
