/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.provisioning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextIssueTypeEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextProjectEntity;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextIssueTypePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextProjectPort;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.domain.port.store.IFieldConfigItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemePort;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.domain.port.store.IScreenTabPort;
import serp.project.pmcore.domain.service.provisioning.materializer.CustomFieldMaterializer;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCustomFieldContextProvisioningService {

    private static final String CUSTOM_FIELD_REF_TYPE = "CUSTOM";
    private static final Set<ProvisioningMode> LOCAL_CONTEXT_MODES = Set.of(
            ProvisioningMode.TEMPLATE_DEFAULT,
            ProvisioningMode.CLONE_FROM_SHARED
    );

    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IIssueTypePort issueTypePort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IFieldConfigItemPort fieldConfigItemPort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;
    private final ICustomFieldPort customFieldPort;
    private final ICustomFieldContextPort customFieldContextPort;
    private final ICustomFieldContextProjectPort customFieldContextProjectPort;
    private final ICustomFieldContextIssueTypePort customFieldContextIssueTypePort;
    private final ICustomFieldOptionPort customFieldOptionPort;
    private final ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    private final CustomFieldMaterializer customFieldMaterializer;
    private final IssueTypeMaterializer issueTypeMaterializer;

    public void provision(ProjectEntity project,
                          ProjectProvisioningRequest request,
                          Map<SchemeType, Long> effectiveBindings) {
        validateArguments(project, request, effectiveBindings);

        ProvisioningMode provisioningMode = request.getEffectiveProvisioningMode();
        if (!LOCAL_CONTEXT_MODES.contains(provisioningMode)) {
            return;
        }

        Set<Long> effectiveIssueTypeIds = resolveEffectiveIssueTypeIds(
                requireBinding(effectiveBindings, SchemeType.ISSUE_TYPE),
                request.getTenantId()
        );
        if (effectiveIssueTypeIds.isEmpty()) {
            return;
        }

        Set<String> relevantFieldKeys = collectRelevantCustomFieldKeys(effectiveBindings, effectiveIssueTypeIds, request.getTenantId());
        for (String fieldKey : relevantFieldKeys) {
            provisionFieldContextTree(project, fieldKey, effectiveIssueTypeIds, request.getTenantId(), request.getUserId());
        }
    }

    private void provisionFieldContextTree(ProjectEntity project,
                                           String fieldKey,
                                           Set<Long> effectiveIssueTypeIds,
                                           Long tenantId,
                                           Long userId) {
        List<CustomFieldEntity> candidates = customFieldPort.getCustomFieldsByFieldKeysIncludingSystem(List.of(fieldKey), tenantId);
        CustomFieldEntity sourceField = resolveSourceCustomField(fieldKey, candidates, tenantId);
        Long targetCustomFieldId = customFieldMaterializer.materialize(sourceField.getId(), tenantId, userId);

        List<CustomFieldContextEntity> sourceContexts = customFieldContextPort
                .getCustomFieldContextsByCustomFieldIdIncludingSystem(sourceField.getId(), tenantId);
        if (sourceContexts.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "No custom field contexts found for field=" + fieldKey
            );
        }

        for (CustomFieldContextEntity sourceContext : sourceContexts) {
            Set<Long> mappedIssueTypeIds = mapSourceIssueTypes(sourceContext, tenantId, userId);
            if (!isContextRelevant(sourceContext, mappedIssueTypeIds, effectiveIssueTypeIds)) {
                continue;
            }

            String targetContextName = buildTargetContextName(sourceContext, project.getKey());
            CustomFieldContextEntity targetContext = customFieldContextPort
                    .getCustomFieldContextByName(targetCustomFieldId, targetContextName, tenantId)
                    .orElseGet(() -> createTargetContext(sourceContext, targetCustomFieldId, targetContextName, tenantId, userId));

            ensureContextChildren(sourceContext, targetContext, project, mappedIssueTypeIds, tenantId, userId);
        }

        validateTargetContexts(fieldKey, targetCustomFieldId, project.getId(), effectiveIssueTypeIds, tenantId);
    }

    private Set<Long> resolveEffectiveIssueTypeIds(Long issueTypeSchemeId, Long tenantId) {
        Set<Long> issueTypeIds = new LinkedHashSet<>();
        for (var item : issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(issueTypeSchemeId, tenantId)) {
            issueTypePort.getIssueTypeById(item.getIssueTypeId(), tenantId)
                    .orElseThrow(() -> ResourceNotFoundException.issueType(item.getIssueTypeId()));
            issueTypeIds.add(item.getIssueTypeId());
        }
        return issueTypeIds;
    }

    private Set<String> collectRelevantCustomFieldKeys(Map<SchemeType, Long> effectiveBindings,
                                                       Set<Long> effectiveIssueTypeIds,
                                                       Long tenantId) {
        Set<String> fieldKeys = new LinkedHashSet<>();
        fieldKeys.addAll(collectFieldConfigCustomFieldKeys(requireBinding(effectiveBindings, SchemeType.FIELD_CONFIG), effectiveIssueTypeIds, tenantId));
        fieldKeys.addAll(collectScreenCustomFieldKeys(requireBinding(effectiveBindings, SchemeType.SCREEN), effectiveIssueTypeIds, tenantId));
        return fieldKeys;
    }

    private Set<String> collectFieldConfigCustomFieldKeys(Long fieldConfigSchemeId,
                                                          Set<Long> effectiveIssueTypeIds,
                                                          Long tenantId) {
        FieldConfigSchemeEntity scheme = fieldConfigSchemePort.getFieldConfigSchemeById(fieldConfigSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found in tenant scope: id=" + fieldConfigSchemeId
                ));

        Map<Long, Long> issueTypeFieldConfigMap = new HashMap<>();
        for (FieldConfigSchemeItemEntity item : fieldConfigSchemeItemPort.getFieldConfigSchemeItemsBySchemeId(fieldConfigSchemeId, tenantId)) {
            issueTypeFieldConfigMap.put(item.getIssueTypeId(), item.getFieldConfigId());
        }

        Set<String> fieldKeys = new LinkedHashSet<>();
        Set<Long> processedFieldConfigs = new HashSet<>();
        for (Long issueTypeId : effectiveIssueTypeIds) {
            Long fieldConfigId = issueTypeFieldConfigMap.get(issueTypeId);
            if (fieldConfigId == null) {
                fieldConfigId = scheme.getDefaultFieldConfigId();
            }
            if (fieldConfigId == null || !processedFieldConfigs.add(fieldConfigId)) {
                continue;
            }

            for (FieldConfigItemEntity item : fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(fieldConfigId, tenantId)) {
                if (isCustomFieldRef(item.getFieldRefType(), item.getFieldRef())) {
                    fieldKeys.add(item.getFieldRef());
                }
            }
        }

        return fieldKeys;
    }

    private Set<String> collectScreenCustomFieldKeys(Long issueTypeScreenSchemeId,
                                                     Set<Long> effectiveIssueTypeIds,
                                                     Long tenantId) {
        IssueTypeScreenSchemeEntity scheme = issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(issueTypeScreenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found in tenant scope: id=" + issueTypeScreenSchemeId
                ));

        Map<Long, Long> issueTypeScreenSchemeMap = new HashMap<>();
        for (IssueTypeScreenSchemeItemEntity item : issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(issueTypeScreenSchemeId, tenantId)) {
            issueTypeScreenSchemeMap.put(item.getIssueTypeId(), item.getScreenSchemeId());
        }

        Set<String> fieldKeys = new LinkedHashSet<>();
        Set<Long> processedScreenSchemes = new HashSet<>();
        for (Long issueTypeId : effectiveIssueTypeIds) {
            Long screenSchemeId = issueTypeScreenSchemeMap.get(issueTypeId);
            if (screenSchemeId == null) {
                screenSchemeId = scheme.getDefaultScreenSchemeId();
            }
            if (screenSchemeId == null || !processedScreenSchemes.add(screenSchemeId)) {
                continue;
            }

            fieldKeys.addAll(collectScreenSchemeCustomFieldKeys(screenSchemeId, tenantId));
        }

        return fieldKeys;
    }

    private Set<String> collectScreenSchemeCustomFieldKeys(Long screenSchemeId, Long tenantId) {
        ScreenSchemeEntity screenScheme = screenSchemePort.getScreenSchemeById(screenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found in tenant scope: id=" + screenSchemeId
                ));

        Set<Long> screenIds = new LinkedHashSet<>();
        if (screenScheme.getDefaultScreenId() != null) {
            screenIds.add(screenScheme.getDefaultScreenId());
        }
        for (ScreenSchemeItemEntity item : screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(screenSchemeId, tenantId)) {
            if (item.getScreenId() != null) {
                screenIds.add(item.getScreenId());
            }
        }

        Set<String> fieldKeys = new LinkedHashSet<>();
        for (Long screenId : screenIds) {
            List<ScreenTabEntity> tabs = screenTabPort.getScreenTabsByScreenId(screenId, tenantId);
            for (ScreenTabEntity tab : tabs) {
                for (ScreenTabFieldEntity field : screenTabFieldPort.getScreenTabFieldsByScreenTabId(tab.getId(), tenantId)) {
                    if (isCustomFieldRef(field.getFieldRefType(), field.getFieldRef())) {
                        fieldKeys.add(field.getFieldRef());
                    }
                }
            }
        }

        return fieldKeys;
    }

    private boolean isCustomFieldRef(String fieldRefType, String fieldRef) {
        return fieldRef != null
                && !fieldRef.isBlank()
                && CUSTOM_FIELD_REF_TYPE.equalsIgnoreCase(fieldRefType == null ? "" : fieldRefType.trim());
    }

    private CustomFieldEntity resolveSourceCustomField(String fieldKey,
                                                       List<CustomFieldEntity> candidates,
                                                       Long tenantId) {
        if (candidates == null || candidates.isEmpty()) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.CUSTOM_FIELD_NOT_FOUND,
                    "Custom field not found for key=" + fieldKey
            );
        }

        CustomFieldEntity tenantCandidate = null;
        CustomFieldEntity systemCandidate = null;
        for (CustomFieldEntity candidate : candidates) {
            if (tenantId.equals(candidate.getTenantId())) {
                tenantCandidate = candidate;
            } else if (Long.valueOf(0L).equals(candidate.getTenantId())) {
                systemCandidate = candidate;
            }
        }

        if (tenantCandidate != null) {
            List<CustomFieldContextEntity> tenantContexts = customFieldContextPort
                    .getCustomFieldContextsByCustomFieldIdIncludingSystem(tenantCandidate.getId(), tenantId);
            if (!tenantContexts.isEmpty()) {
                return tenantCandidate;
            }
        }

        if (systemCandidate != null) {
            return systemCandidate;
        }

        return tenantCandidate;
    }

    private Set<Long> mapSourceIssueTypes(CustomFieldContextEntity sourceContext,
                                          Long tenantId,
                                          Long userId) {
        if (Boolean.TRUE.equals(sourceContext.getAppliesToAllIssueTypes())) {
            return Collections.emptySet();
        }

        Set<Long> issueTypeIds = new LinkedHashSet<>();
        for (CustomFieldContextIssueTypeEntity contextIssueType : customFieldContextIssueTypePort
                .getCustomFieldContextIssueTypesByContextIdIncludingSystem(sourceContext.getId(), tenantId)) {
            issueTypeIds.add(issueTypeMaterializer.materialize(contextIssueType.getIssueTypeId(), tenantId, userId));
        }

        if (issueTypeIds.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Custom field context has no issue type mappings: contextId=" + sourceContext.getId()
            );
        }

        return issueTypeIds;
    }

    private boolean isContextRelevant(CustomFieldContextEntity sourceContext,
                                      Set<Long> mappedIssueTypeIds,
                                      Set<Long> effectiveIssueTypeIds) {
        if (Boolean.TRUE.equals(sourceContext.getAppliesToAllIssueTypes())) {
            return true;
        }
        for (Long issueTypeId : mappedIssueTypeIds) {
            if (effectiveIssueTypeIds.contains(issueTypeId)) {
                return true;
            }
        }
        return false;
    }

    private String buildTargetContextName(CustomFieldContextEntity sourceContext, String projectKey) {
        if (Boolean.TRUE.equals(sourceContext.getAppliesToAllProjects())) {
            return sourceContext.getName();
        }
        return sourceContext.getName() + " (" + projectKey + ")";
    }

    private CustomFieldContextEntity createTargetContext(CustomFieldContextEntity sourceContext,
                                                         Long targetCustomFieldId,
                                                         String targetContextName,
                                                         Long tenantId,
                                                         Long userId) {
        long now = System.currentTimeMillis();
        CustomFieldContextEntity targetContext = CustomFieldContextEntity.builder()
                .tenantId(tenantId)
                .customFieldId(targetCustomFieldId)
                .name(targetContextName)
                .description(sourceContext.getDescription())
                .isGlobalContext(sourceContext.getIsGlobalContext())
                .appliesToAllProjects(sourceContext.getAppliesToAllProjects())
                .appliesToAllIssueTypes(sourceContext.getAppliesToAllIssueTypes())
                .build();
        targetContext.applyCreate(userId, now);
        return customFieldContextPort.createCustomFieldContexts(List.of(targetContext)).getFirst();
    }

    private void ensureContextChildren(CustomFieldContextEntity sourceContext,
                                       CustomFieldContextEntity targetContext,
                                       ProjectEntity project,
                                       Set<Long> mappedIssueTypeIds,
                                       Long tenantId,
                                       Long userId) {
        if (!Boolean.TRUE.equals(targetContext.getAppliesToAllProjects())
                && customFieldContextProjectPort.getCustomFieldContextProjectsByContextId(targetContext.getId(), tenantId).isEmpty()) {
            long now = System.currentTimeMillis();
            CustomFieldContextProjectEntity contextProject = CustomFieldContextProjectEntity.builder()
                    .tenantId(tenantId)
                    .contextId(targetContext.getId())
                    .projectId(project.getId())
                    .build();
            contextProject.applyCreate(userId, now);
            customFieldContextProjectPort.createCustomFieldContextProjects(List.of(contextProject));
        }

        if (!Boolean.TRUE.equals(targetContext.getAppliesToAllIssueTypes())
                && customFieldContextIssueTypePort.getCustomFieldContextIssueTypesByContextId(targetContext.getId(), tenantId).isEmpty()) {
            long now = System.currentTimeMillis();
            List<CustomFieldContextIssueTypeEntity> targetIssueTypes = new ArrayList<>();
            for (Long issueTypeId : mappedIssueTypeIds) {
                CustomFieldContextIssueTypeEntity contextIssueType = CustomFieldContextIssueTypeEntity.builder()
                        .tenantId(tenantId)
                        .contextId(targetContext.getId())
                        .issueTypeId(issueTypeId)
                        .build();
                contextIssueType.applyCreate(userId, now);
                targetIssueTypes.add(contextIssueType);
            }
            customFieldContextIssueTypePort.createCustomFieldContextIssueTypes(targetIssueTypes);
        }

        ensureContextOptionsAndDefaults(sourceContext, targetContext, tenantId, userId);
    }

    private void ensureContextOptionsAndDefaults(CustomFieldContextEntity sourceContext,
                                                 CustomFieldContextEntity targetContext,
                                                 Long tenantId,
                                                 Long userId) {
        Map<Long, Long> optionIdMap = new LinkedHashMap<>();
        if (customFieldOptionPort.getCustomFieldOptionsByContextId(targetContext.getId(), tenantId).isEmpty()) {
            optionIdMap = cloneContextOptions(sourceContext, targetContext, tenantId, userId);
        }

        if (customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(targetContext.getId(), tenantId).isEmpty()) {
            cloneContextDefaultValues(sourceContext, targetContext, optionIdMap, tenantId, userId);
        }
    }

    private Map<Long, Long> cloneContextOptions(CustomFieldContextEntity sourceContext,
                                                CustomFieldContextEntity targetContext,
                                                Long tenantId,
                                                Long userId) {
        List<CustomFieldOptionEntity> sourceOptions = customFieldOptionPort
                .getCustomFieldOptionsByContextIdIncludingSystem(sourceContext.getId(), tenantId);
        if (sourceOptions.isEmpty()) {
            return Collections.emptyMap();
        }

        long now = System.currentTimeMillis();
        List<CustomFieldOptionEntity> pendingOptions = new ArrayList<>();
        for (CustomFieldOptionEntity sourceOption : sourceOptions) {
            CustomFieldOptionEntity targetOption = CustomFieldOptionEntity.builder()
                    .tenantId(tenantId)
                    .customFieldContextId(targetContext.getId())
                    .optionKey(sourceOption.getOptionKey())
                    .value(sourceOption.getValue())
                    .sequence(sourceOption.getSequence())
                    .parentOptionId(null)
                    .isDisabled(sourceOption.getIsDisabled())
                    .build();
            targetOption.applyCreate(userId, now);
            pendingOptions.add(targetOption);
        }

        List<CustomFieldOptionEntity> savedOptions = customFieldOptionPort.createCustomFieldOptions(pendingOptions);
        Map<Long, Long> optionIdMap = new LinkedHashMap<>();
        for (int index = 0; index < sourceOptions.size(); index++) {
            optionIdMap.put(sourceOptions.get(index).getId(), savedOptions.get(index).getId());
        }

        List<CustomFieldOptionEntity> optionsToUpdate = new ArrayList<>();
        for (int index = 0; index < sourceOptions.size(); index++) {
            Long sourceParentId = sourceOptions.get(index).getParentOptionId();
            if (sourceParentId == null) {
                continue;
            }
            CustomFieldOptionEntity savedOption = savedOptions.get(index);
            savedOption.setParentOptionId(optionIdMap.get(sourceParentId));
            savedOption.applyUpdate(userId, System.currentTimeMillis());
            optionsToUpdate.add(savedOption);
        }

        if (!optionsToUpdate.isEmpty()) {
            customFieldOptionPort.createCustomFieldOptions(optionsToUpdate);
        }

        return optionIdMap;
    }

    private void cloneContextDefaultValues(CustomFieldContextEntity sourceContext,
                                           CustomFieldContextEntity targetContext,
                                           Map<Long, Long> optionIdMap,
                                           Long tenantId,
                                           Long userId) {
        List<CustomFieldContextDefaultValueEntity> sourceDefaults = customFieldContextDefaultValuePort
                .getCustomFieldContextDefaultValuesByContextIdIncludingSystem(sourceContext.getId(), tenantId);
        if (sourceDefaults.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<CustomFieldContextDefaultValueEntity> targetDefaults = new ArrayList<>();
        for (CustomFieldContextDefaultValueEntity sourceDefault : sourceDefaults) {
            Long mappedOptionId = sourceDefault.getOptionValueId() == null
                    ? null
                    : optionIdMap.get(sourceDefault.getOptionValueId());

            CustomFieldContextDefaultValueEntity targetDefault = CustomFieldContextDefaultValueEntity.builder()
                    .tenantId(tenantId)
                    .contextId(targetContext.getId())
                    .valueType(sourceDefault.getValueType())
                    .textValue(sourceDefault.getTextValue())
                    .numberValue(sourceDefault.getNumberValue())
                    .dateValue(sourceDefault.getDateValue())
                    .datetimeValue(sourceDefault.getDatetimeValue())
                    .userValueId(sourceDefault.getUserValueId())
                    .groupValueId(sourceDefault.getGroupValueId())
                    .optionValueId(mappedOptionId)
                    .jsonValue(sourceDefault.getJsonValue())
                    .sortOrder(sourceDefault.getSortOrder())
                    .build();
            targetDefault.applyCreate(userId, now);
            targetDefaults.add(targetDefault);
        }

        customFieldContextDefaultValuePort.createCustomFieldContextDefaultValues(targetDefaults);
    }

    private void validateTargetContexts(String fieldKey,
                                        Long targetCustomFieldId,
                                        Long projectId,
                                        Set<Long> effectiveIssueTypeIds,
                                        Long tenantId) {
        List<CustomFieldContextEntity> contexts = customFieldContextPort
                .getCustomFieldContextsByCustomFieldIdIncludingSystem(targetCustomFieldId, tenantId);

        Map<Long, Set<Long>> contextProjects = new HashMap<>();
        Map<Long, Set<Long>> contextIssueTypes = new HashMap<>();
        for (Long issueTypeId : effectiveIssueTypeIds) {
            List<CustomFieldContextEntity> applicableContexts = new ArrayList<>();
            for (CustomFieldContextEntity context : contexts) {
                if (appliesToProject(context, projectId, tenantId, contextProjects)
                        && appliesToIssueType(context, issueTypeId, tenantId, contextIssueTypes)) {
                    applicableContexts.add(context);
                }
            }

            if (applicableContexts.isEmpty()) {
                throw new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "No custom field context resolves for field=" + fieldKey
                                + ", projectId=" + projectId
                                + ", issueTypeId=" + issueTypeId
                );
            }

            Map<Integer, List<CustomFieldContextEntity>> contextsBySpecificity = new HashMap<>();
            for (CustomFieldContextEntity applicableContext : applicableContexts) {
                contextsBySpecificity.computeIfAbsent(contextSpecificity(applicableContext), ignored -> new ArrayList<>())
                        .add(applicableContext);
            }

            Integer bestSpecificity = contextsBySpecificity.keySet().stream().min(Integer::compareTo)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                            "Unable to determine custom field context specificity for field=" + fieldKey
                    ));

            List<CustomFieldContextEntity> bestContexts = contextsBySpecificity.get(bestSpecificity);
            if (bestContexts == null || bestContexts.size() != 1) {
                throw new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Custom field context is ambiguous for field=" + fieldKey
                                + ", projectId=" + projectId
                                + ", issueTypeId=" + issueTypeId
                );
            }
        }
    }

    private boolean appliesToProject(CustomFieldContextEntity context,
                                     Long projectId,
                                     Long tenantId,
                                     Map<Long, Set<Long>> contextProjects) {
        if (Boolean.TRUE.equals(context.getAppliesToAllProjects())) {
            return true;
        }
        return contextProjects.computeIfAbsent(
                context.getId(),
                ignored -> loadProjectIds(context.getId(), tenantId)
        ).contains(projectId);
    }

    private boolean appliesToIssueType(CustomFieldContextEntity context,
                                       Long issueTypeId,
                                       Long tenantId,
                                       Map<Long, Set<Long>> contextIssueTypes) {
        if (Boolean.TRUE.equals(context.getAppliesToAllIssueTypes())) {
            return true;
        }
        return contextIssueTypes.computeIfAbsent(
                context.getId(),
                ignored -> loadIssueTypeIds(context.getId(), tenantId)
        ).contains(issueTypeId);
    }

    private Set<Long> loadProjectIds(Long contextId, Long tenantId) {
        Set<Long> projectIds = new HashSet<>();
        for (CustomFieldContextProjectEntity project : customFieldContextProjectPort.getCustomFieldContextProjectsByContextId(contextId, tenantId)) {
            projectIds.add(project.getProjectId());
        }
        return projectIds;
    }

    private Set<Long> loadIssueTypeIds(Long contextId, Long tenantId) {
        Set<Long> issueTypeIds = new HashSet<>();
        for (CustomFieldContextIssueTypeEntity issueType : customFieldContextIssueTypePort.getCustomFieldContextIssueTypesByContextId(contextId, tenantId)) {
            issueTypeIds.add(issueType.getIssueTypeId());
        }
        return issueTypeIds;
    }

    private int contextSpecificity(CustomFieldContextEntity context) {
        boolean allProjects = Boolean.TRUE.equals(context.getAppliesToAllProjects());
        boolean allIssueTypes = Boolean.TRUE.equals(context.getAppliesToAllIssueTypes());
        if (!allProjects && !allIssueTypes) {
            return 0;
        }
        if (!allProjects) {
            return 1;
        }
        if (!allIssueTypes) {
            return 2;
        }
        return 3;
    }

    private Long requireBinding(Map<SchemeType, Long> effectiveBindings, SchemeType schemeType) {
        Long bindingId = effectiveBindings.get(schemeType);
        if (bindingId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing effective scheme binding for type=" + schemeType
            );
        }
        return bindingId;
    }

    private void validateArguments(ProjectEntity project,
                                   ProjectProvisioningRequest request,
                                   Map<SchemeType, Long> effectiveBindings) {
        Objects.requireNonNull(project, "Project must not be null");
        Objects.requireNonNull(request, "Project provisioning request must not be null");
        Objects.requireNonNull(effectiveBindings, "Effective bindings must not be null");
    }
}
