/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.PriorityEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.entity.StatusEntity;
import serp.project.pmcore.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.entity.TenantWorkflowMappingEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.AppException;
import serp.project.pmcore.domain.exception.ErrorCode;
import serp.project.pmcore.domain.port.store.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemePort;
import serp.project.pmcore.domain.port.store.IPriorityPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IStatusCategoryPort;
import serp.project.pmcore.domain.port.store.IStatusPort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.port.store.ITenantWorkflowMappingPort;
import serp.project.pmcore.domain.port.store.IWorkflowPort;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowTransitionPort;
import serp.project.pmcore.domain.port.store.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles scheme provisioning for project creation.
 *
 * Shared-association default strategy:
 * - If source scheme already belongs to tenant, bind directly.
 * - If source scheme is system (tenant_id=0), clone once to tenant-shared scheme,
 *   store mapping (tenant, scheme_type, source_scheme_id -> tenant_scheme_id),
 *   then bind project to tenant-shared scheme.
 * - If mapping already exists and target scheme is still present, reuse it.
 *
 * ISSUE_TYPE/PRIORITY/WORKFLOW are implemented.
 * For system-sourced schemes, dictionary/workflow graph rows are
 * materialized into tenant scope and scheme items/default IDs are remapped.
 * Other scheme families remain pending and are currently logged as stubs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeProvisioningService implements ISchemeProvisioningService {

    private static final Long SYSTEM_TENANT_ID = 0L;

    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IPriorityPort priorityPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IStatusCategoryPort statusCategoryPort;
    private final IStatusPort statusPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IWorkflowTransitionRulePort workflowTransitionRulePort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final ITenantSchemeMappingPort tenantSchemeMappingPort;
    private final ITenantWorkflowMappingPort tenantWorkflowMappingPort;

    @Override
    public void provisionSchemes(ProjectEntity project, Long tenantId, Long userId,
                                 Long blueprintId, Map<String, Long> schemeOverrides, String associationMode) {
        Map<SchemeType, Long> blueprintDefaults = loadBlueprintDefaults(blueprintId, tenantId);
        Map<SchemeType, Long> resolvedSources = resolveTemplateSources(schemeOverrides, blueprintDefaults);

        String mode = associationMode == null ? "SHARED_ASSOCIATION" : associationMode.toUpperCase(Locale.ROOT);
        boolean cloneOnAssociate = "CLONE_ON_ASSOCIATE".equals(mode);

        Long issueTypeSourceId = requireSourceSchemeId(resolvedSources, SchemeType.ISSUE_TYPE);
        Long prioritySourceId = requireSourceSchemeId(resolvedSources, SchemeType.PRIORITY);
        Long workflowSourceId = requireSourceSchemeId(resolvedSources, SchemeType.WORKFLOW);

        if (cloneOnAssociate) {
            project.setIssueTypeSchemeId(resolveClonedSchemeBinding(SchemeType.ISSUE_TYPE, issueTypeSourceId, tenantId, userId));
            project.setPrioritySchemeId(resolveClonedSchemeBinding(SchemeType.PRIORITY, prioritySourceId, tenantId, userId));
            project.setWorkflowSchemeId(resolveClonedSchemeBinding(SchemeType.WORKFLOW, workflowSourceId, tenantId, userId));
        } else {
            project.setIssueTypeSchemeId(resolveSharedSchemeBinding(SchemeType.ISSUE_TYPE, issueTypeSourceId, tenantId, userId));
            project.setPrioritySchemeId(resolveSharedSchemeBinding(SchemeType.PRIORITY, prioritySourceId, tenantId, userId));
            project.setWorkflowSchemeId(resolveSharedSchemeBinding(SchemeType.WORKFLOW, workflowSourceId, tenantId, userId));
        }

        log.info("Provisioned core {} scheme bindings for project key={}: issueTypeSchemeId={}, prioritySchemeId={}, workflowSchemeId={}",
                cloneOnAssociate ? "CLONE" : "SHARED",
                project.getKey(), project.getIssueTypeSchemeId(), project.getPrioritySchemeId(), project.getWorkflowSchemeId());

        // Stub provisioning for scheme types without infrastructure
        stubProvision("FIELD_CONFIG", SchemeType.FIELD_CONFIG, resolvedSources, project);
        stubProvision("SCREEN", SchemeType.SCREEN, resolvedSources, project);
        stubProvision("PERMISSION", SchemeType.PERMISSION, resolvedSources, project);
        stubProvision("ISSUE_SECURITY", SchemeType.ISSUE_SECURITY, resolvedSources, project);
        stubProvision("NOTIFICATION", SchemeType.NOTIFICATION, resolvedSources, project);
    }

    @Override
    public Long resolveSharedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId) {
        if (schemeType == null || sourceSchemeId == null) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Scheme type and source scheme id are required"
            );
        }

        return switch (schemeType) {
            case ISSUE_TYPE -> resolveIssueTypeSchemeBinding(sourceSchemeId, tenantId, userId);
            case PRIORITY -> resolvePrioritySchemeBinding(sourceSchemeId, tenantId, userId);
            case WORKFLOW -> resolveWorkflowSchemeBinding(sourceSchemeId, tenantId, userId);
            default -> throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Shared association provisioning is not implemented for scheme type " + schemeType
            );
        };
    }

    @Override
    public Long resolveClonedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId) {
        if (schemeType == null || sourceSchemeId == null) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Scheme type and source scheme id are required"
            );
        }

        return switch (schemeType) {
            case ISSUE_TYPE -> resolveIssueTypeSchemeCloneBinding(sourceSchemeId, tenantId, userId);
            case PRIORITY -> resolvePrioritySchemeCloneBinding(sourceSchemeId, tenantId, userId);
            case WORKFLOW -> resolveWorkflowSchemeCloneBinding(sourceSchemeId, tenantId, userId);
            default -> throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Clone-on-associate provisioning is not implemented for scheme type " + schemeType
            );
        };
    }

    private Long resolveIssueTypeSchemeCloneBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        IssueTypeSchemeEntity source = issueTypeSchemePort
                .getIssueTypeSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return cloneIssueTypeSchemeForTenant(source, tenantId, userId, "CLONE");
    }

    private Long resolvePrioritySchemeCloneBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        PrioritySchemeEntity source = prioritySchemePort
                .getPrioritySchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return clonePrioritySchemeForTenant(source, tenantId, userId, "CLONE");
    }

    private Long resolveWorkflowSchemeCloneBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        WorkflowSchemeEntity source = workflowSchemePort
                .getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return cloneWorkflowSchemeForTenant(source, tenantId, userId, "CLONE", false);
    }

    private Map<SchemeType, Long> loadBlueprintDefaults(Long blueprintId, Long tenantId) {
        if (blueprintId == null) {
            return Collections.emptyMap();
        }

        List<BlueprintSchemeDefaultEntity> defaults =
                blueprintSchemeDefaultPort.getDefaultsByBlueprintIdIncludingSystem(blueprintId, tenantId);

        return defaults.stream()
                .filter(d -> d.getSchemeType() != null && d.getSchemeId() != null)
                .collect(Collectors.toMap(
                        BlueprintSchemeDefaultEntity::getSchemeType,
                        BlueprintSchemeDefaultEntity::getSchemeId,
                        (a, b) -> a
                ));
    }

    private Map<SchemeType, Long> resolveTemplateSources(Map<String, Long> overrides,
                                                          Map<SchemeType, Long> blueprintDefaults) {
        Map<SchemeType, Long> resolved = new EnumMap<>(SchemeType.class);

        for (SchemeType type : SchemeType.values()) {
            if (overrides != null && overrides.containsKey(type.toString())) {
                resolved.put(type, overrides.get(type.toString()));
                continue;
            }

            if (blueprintDefaults.containsKey(type)) {
                resolved.put(type, blueprintDefaults.get(type));
            }
        }

        return resolved;
    }

    private Long requireSourceSchemeId(Map<SchemeType, Long> resolvedSources, SchemeType type) {
        Long sourceId = resolvedSources.get(type);
        if (sourceId == null) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing source scheme for " + type + ". Provide explicit override or blueprint default"
            );
        }
        return sourceId;
    }

    private Long resolveIssueTypeSchemeBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        IssueTypeSchemeEntity source = issueTypeSchemePort
                .getIssueTypeSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }
        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(ErrorCode.SCHEME_NOT_FOUND);
        }

        Optional<TenantSchemeMappingEntity> mapping = tenantSchemeMappingPort
                .getMapping(tenantId, SchemeType.ISSUE_TYPE, sourceSchemeId);

        if (mapping.isPresent()) {
            Long tenantSchemeId = mapping.get().getTenantSchemeId();
            if (issueTypeSchemePort.getIssueTypeSchemeById(tenantSchemeId, tenantId).isPresent()) {
                return tenantSchemeId;
            }
            log.warn("Stale ISSUE_TYPE mapping found (tenantId={}, sourceSchemeId={}, mappedSchemeId={})",
                    tenantId, sourceSchemeId, tenantSchemeId);
        }

        Long clonedSchemeId = cloneIssueTypeSchemeForTenant(source, tenantId, userId, "SHARED");
        upsertTenantSchemeMapping(mapping, tenantId, userId, SchemeType.ISSUE_TYPE, sourceSchemeId, clonedSchemeId);
        return clonedSchemeId;
    }

    private Long resolvePrioritySchemeBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        PrioritySchemeEntity source = prioritySchemePort
                .getPrioritySchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }
        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(ErrorCode.SCHEME_NOT_FOUND);
        }

        Optional<TenantSchemeMappingEntity> mapping = tenantSchemeMappingPort
                .getMapping(tenantId, SchemeType.PRIORITY, sourceSchemeId);

        if (mapping.isPresent()) {
            Long tenantSchemeId = mapping.get().getTenantSchemeId();
            if (prioritySchemePort.getPrioritySchemeById(tenantSchemeId, tenantId).isPresent()) {
                return tenantSchemeId;
            }
            log.warn("Stale PRIORITY mapping found (tenantId={}, sourceSchemeId={}, mappedSchemeId={})",
                    tenantId, sourceSchemeId, tenantSchemeId);
        }

        Long clonedSchemeId = clonePrioritySchemeForTenant(source, tenantId, userId, "SHARED");
        upsertTenantSchemeMapping(mapping, tenantId, userId, SchemeType.PRIORITY, sourceSchemeId, clonedSchemeId);
        return clonedSchemeId;
    }

    private Long resolveWorkflowSchemeBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        WorkflowSchemeEntity source = workflowSchemePort
                .getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }
        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(ErrorCode.SCHEME_NOT_FOUND);
        }

        Optional<TenantSchemeMappingEntity> mapping = tenantSchemeMappingPort
                .getMapping(tenantId, SchemeType.WORKFLOW, sourceSchemeId);

        if (mapping.isPresent()) {
            Long tenantSchemeId = mapping.get().getTenantSchemeId();
            if (workflowSchemePort.getWorkflowSchemeById(tenantSchemeId, tenantId).isPresent()) {
                return tenantSchemeId;
            }
            log.warn("Stale WORKFLOW mapping found (tenantId={}, sourceSchemeId={}, mappedSchemeId={})",
                    tenantId, sourceSchemeId, tenantSchemeId);
        }

        Long clonedSchemeId = cloneWorkflowSchemeForTenant(source, tenantId, userId, "SHARED", true);
        upsertTenantSchemeMapping(mapping, tenantId, userId, SchemeType.WORKFLOW, sourceSchemeId, clonedSchemeId);
        return clonedSchemeId;
    }

    private Long cloneIssueTypeSchemeForTenant(IssueTypeSchemeEntity source, Long tenantId, Long userId, String cloneMode) {
        List<IssueTypeSchemeItemEntity> sourceItems = issueTypeSchemeItemPort
                .getIssueTypeSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> sourceIssueTypeIds = new HashSet<>();
        sourceItems.stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .forEach(sourceIssueTypeIds::add);
        if (source.getDefaultIssueTypeId() != null) {
            sourceIssueTypeIds.add(source.getDefaultIssueTypeId());
        }
        for (Long sourceIssueTypeId : sourceIssueTypeIds) {
            issueTypeIdMap.put(sourceIssueTypeId,
                    materializeIssueTypeForTenant(sourceIssueTypeId, tenantId, userId));
        }

        IssueTypeSchemeEntity cloned = IssueTypeSchemeEntity.builder()
                .tenantId(tenantId)
                .name(buildSchemeCloneName(source.getName(), source.getId(), cloneMode))
                .description(buildSchemeCloneDescription(source.getDescription(), source.getId(), cloneMode))
                .defaultIssueTypeId(requireMappedId(issueTypeIdMap, source.getDefaultIssueTypeId(), "issue type"))
                .build();
        cloned.setCreatedAt(System.currentTimeMillis());
        cloned.setUpdatedAt(System.currentTimeMillis());
        cloned.setCreatedBy(userId);
        cloned.setUpdatedBy(userId);

        IssueTypeSchemeEntity saved = issueTypeSchemePort.createIssueTypeScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<IssueTypeSchemeItemEntity> clonedItems = sourceItems.stream()
                    .map(item -> IssueTypeSchemeItemEntity.builder()
                            .tenantId(tenantId)
                            .schemeId(saved.getId())
                            .issueTypeId(requireMappedId(issueTypeIdMap, item.getIssueTypeId(), "issue type"))
                            .sequence(item.getSequence())
                            .build())
                    .collect(Collectors.toList());
            issueTypeSchemeItemPort.createIssueTypeSchemeItems(clonedItems);
        }

        log.info("Created {} ISSUE_TYPE scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);
        return saved.getId();
    }

    private Long clonePrioritySchemeForTenant(PrioritySchemeEntity source, Long tenantId, Long userId, String cloneMode) {
        List<PrioritySchemeItemEntity> sourceItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> priorityIdMap = new HashMap<>();
        Set<Long> sourcePriorityIds = new HashSet<>();
        sourceItems.stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .forEach(sourcePriorityIds::add);
        if (source.getDefaultPriorityId() != null) {
            sourcePriorityIds.add(source.getDefaultPriorityId());
        }
        for (Long sourcePriorityId : sourcePriorityIds) {
            priorityIdMap.put(sourcePriorityId,
                    materializePriorityForTenant(sourcePriorityId, tenantId, userId));
        }

        PrioritySchemeEntity cloned = PrioritySchemeEntity.builder()
                .tenantId(tenantId)
                .name(buildSchemeCloneName(source.getName(), source.getId(), cloneMode))
                .description(buildSchemeCloneDescription(source.getDescription(), source.getId(), cloneMode))
                .defaultPriorityId(requireMappedId(priorityIdMap, source.getDefaultPriorityId(), "priority"))
                .build();
        cloned.setCreatedAt(System.currentTimeMillis());
        cloned.setUpdatedAt(System.currentTimeMillis());
        cloned.setCreatedBy(userId);
        cloned.setUpdatedBy(userId);

        PrioritySchemeEntity saved = prioritySchemePort.createPriorityScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<PrioritySchemeItemEntity> clonedItems = sourceItems.stream()
                    .map(item -> PrioritySchemeItemEntity.builder()
                            .tenantId(tenantId)
                            .schemeId(saved.getId())
                            .priorityId(requireMappedId(priorityIdMap, item.getPriorityId(), "priority"))
                            .sequence(item.getSequence())
                            .build())
                    .collect(Collectors.toList());
            prioritySchemeItemPort.createPrioritySchemeItems(clonedItems);
        }

        log.info("Created {} PRIORITY scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);
        return saved.getId();
    }

    private Long cloneWorkflowSchemeForTenant(WorkflowSchemeEntity source,
                                              Long tenantId,
                                              Long userId,
                                              String cloneMode,
                                              boolean reuseSharedWorkflowMapping) {
        List<WorkflowSchemeItemEntity> sourceItems = workflowSchemeItemPort
                .getWorkflowSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> sourceIssueTypeIds = sourceItems.stream()
                .map(WorkflowSchemeItemEntity::getIssueTypeId)
                .collect(Collectors.toSet());
        for (Long sourceIssueTypeId : sourceIssueTypeIds) {
            issueTypeIdMap.put(sourceIssueTypeId,
                    materializeIssueTypeForTenant(sourceIssueTypeId, tenantId, userId));
        }

        Map<Long, Long> workflowIdMap = new HashMap<>();
        Set<Long> sourceWorkflowIds = new HashSet<>();
        sourceItems.stream()
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .forEach(sourceWorkflowIds::add);
        if (source.getDefaultWorkflowId() != null) {
            sourceWorkflowIds.add(source.getDefaultWorkflowId());
        }
        for (Long sourceWorkflowId : sourceWorkflowIds) {
            Long mappedWorkflowId = reuseSharedWorkflowMapping
                    ? materializeWorkflowForTenant(sourceWorkflowId, tenantId, userId)
                    : cloneWorkflowForTenantBySourceId(sourceWorkflowId, tenantId, userId);
            workflowIdMap.put(sourceWorkflowId, mappedWorkflowId);
        }

        WorkflowSchemeEntity cloned = WorkflowSchemeEntity.builder()
                .tenantId(tenantId)
                .name(buildSchemeCloneName(source.getName(), source.getId(), cloneMode))
                .description(buildSchemeCloneDescription(source.getDescription(), source.getId(), cloneMode))
                .defaultWorkflowId(requireMappedId(workflowIdMap, source.getDefaultWorkflowId(), "workflow"))
                .build();
        cloned.setCreatedAt(System.currentTimeMillis());
        cloned.setUpdatedAt(System.currentTimeMillis());
        cloned.setCreatedBy(userId);
        cloned.setUpdatedBy(userId);

        WorkflowSchemeEntity saved = workflowSchemePort.createWorkflowScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<WorkflowSchemeItemEntity> clonedItems = sourceItems.stream()
                    .map(item -> WorkflowSchemeItemEntity.builder()
                            .tenantId(tenantId)
                            .schemeId(saved.getId())
                            .issueTypeId(requireMappedId(issueTypeIdMap, item.getIssueTypeId(), "issue type"))
                            .workflowId(requireMappedId(workflowIdMap, item.getWorkflowId(), "workflow"))
                            .build())
                    .collect(Collectors.toList());
            workflowSchemeItemPort.createWorkflowSchemeItems(clonedItems);
        }

        log.info("Created {} WORKFLOW scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);
        return saved.getId();
    }

    private Long cloneWorkflowForTenantBySourceId(Long sourceWorkflowId, Long tenantId, Long userId) {
        WorkflowEntity source = workflowPort
                .getWorkflowByIdIncludingSystem(sourceWorkflowId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Workflow not found for source id=" + sourceWorkflowId));

        return cloneWorkflowForTenant(source, tenantId, userId, "CLONE");
    }

    private Long materializeIssueTypeForTenant(Long sourceIssueTypeId, Long tenantId, Long userId) {
        IssueTypeEntity source = issueTypePort
                .getIssueTypeByIdIncludingSystem(sourceIssueTypeId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Issue type not found for source id=" + sourceIssueTypeId));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Issue type source is not tenant/system scoped: " + sourceIssueTypeId);
        }

        Optional<IssueTypeEntity> existing = issueTypePort.getIssueTypeByTypeKey(tenantId, source.getTypeKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        long now = System.currentTimeMillis();
        IssueTypeEntity clone = IssueTypeEntity.builder()
                .tenantId(tenantId)
                .typeKey(source.getTypeKey())
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .hierarchyLevel(source.getHierarchyLevel())
                .isSystem(false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        IssueTypeEntity saved = issueTypePort.createIssueType(clone);
        log.info("Materialized ISSUE_TYPE source={} -> tenant={} for tenantId={}",
                sourceIssueTypeId, saved.getId(), tenantId);
        return saved.getId();
    }

    private Long materializePriorityForTenant(Long sourcePriorityId, Long tenantId, Long userId) {
        PriorityEntity source = priorityPort
                .getPriorityByIdIncludingSystem(sourcePriorityId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Priority not found for source id=" + sourcePriorityId));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Priority source is not tenant/system scoped: " + sourcePriorityId);
        }

        String priorityKey = derivePriorityKey(source, sourcePriorityId);
        Optional<PriorityEntity> existing = priorityPort.getPriorityByPriorityKey(tenantId, priorityKey);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        long now = System.currentTimeMillis();
        PriorityEntity clone = PriorityEntity.builder()
                .tenantId(tenantId)
                .priorityKey(priorityKey)
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .color(source.getColor())
                .sequence(source.getSequence())
                .isSystem(false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        PriorityEntity saved = priorityPort.createPriority(clone);
        log.info("Materialized PRIORITY source={} -> tenant={} for tenantId={}",
                sourcePriorityId, saved.getId(), tenantId);
        return saved.getId();
    }

    private Long materializeWorkflowForTenant(Long sourceWorkflowId, Long tenantId, Long userId) {
        WorkflowEntity source = workflowPort
                .getWorkflowByIdIncludingSystem(sourceWorkflowId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Workflow not found for source id=" + sourceWorkflowId));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Workflow source is not tenant/system scoped: " + sourceWorkflowId);
        }

        Optional<TenantWorkflowMappingEntity> mapping = tenantWorkflowMappingPort
                .getMapping(tenantId, sourceWorkflowId);

        if (mapping.isPresent()) {
            Long tenantWorkflowId = mapping.get().getTenantWorkflowId();
            if (workflowPort.getWorkflowById(tenantWorkflowId, tenantId).isPresent()) {
                return tenantWorkflowId;
            }
            log.warn("Stale WORKFLOW materialization mapping found (tenantId={}, sourceWorkflowId={}, mappedWorkflowId={})",
                    tenantId, sourceWorkflowId, tenantWorkflowId);
        }

        Long clonedWorkflowId = cloneWorkflowForTenant(source, tenantId, userId, "SHARED");
        upsertTenantWorkflowMapping(mapping, tenantId, userId, sourceWorkflowId, clonedWorkflowId);
        return clonedWorkflowId;
    }

    private Long cloneWorkflowForTenant(WorkflowEntity source, Long tenantId, Long userId, String cloneMode) {
        List<WorkflowStepEntity> sourceSteps = workflowStepPort
                .getWorkflowStepsByWorkflowIdIncludingSystem(source.getId(), tenantId);
        List<WorkflowEntity.WorkflowTransitionEntity> sourceTransitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> statusIdMap = new HashMap<>();
        Set<Long> sourceStatusIds = new HashSet<>();
        sourceSteps.stream()
                .map(WorkflowStepEntity::getStatusId)
                .filter(id -> id != null)
                .forEach(sourceStatusIds::add);
        sourceTransitions.stream()
                .map(WorkflowEntity.WorkflowTransitionEntity::getFromStatusId)
                .filter(id -> id != null)
                .forEach(sourceStatusIds::add);
        sourceTransitions.stream()
                .map(WorkflowEntity.WorkflowTransitionEntity::getToStatusId)
                .filter(id -> id != null)
                .forEach(sourceStatusIds::add);
        for (Long sourceStatusId : sourceStatusIds) {
            statusIdMap.put(sourceStatusId,
                    materializeStatusForTenant(sourceStatusId, tenantId, userId));
        }

        WorkflowEntity cloned = WorkflowEntity.builder()
                .tenantId(tenantId)
                .name(buildSchemeCloneName(source.getName(), source.getId(), cloneMode))
                .description(buildSchemeCloneDescription(source.getDescription(), source.getId(), cloneMode))
                .versionNo(source.getVersionNo())
                .isActive(source.getIsActive())
                .isSystem(false)
                .build();
        cloned.setCreatedAt(System.currentTimeMillis());
        cloned.setUpdatedAt(System.currentTimeMillis());
        cloned.setCreatedBy(userId);
        cloned.setUpdatedBy(userId);

        WorkflowEntity savedWorkflow = workflowPort.createWorkflow(cloned);

        if (!sourceSteps.isEmpty()) {
            List<WorkflowStepEntity> clonedSteps = sourceSteps.stream()
                    .map(step -> WorkflowStepEntity.builder()
                            .tenantId(tenantId)
                            .workflowId(savedWorkflow.getId())
                            .statusId(requireMappedId(statusIdMap, step.getStatusId(), "status"))
                            .sequence(step.getSequence())
                            .isInitial(step.getIsInitial())
                            .isFinal(step.getIsFinal())
                            .build())
                    .collect(Collectors.toList());
            workflowStepPort.createWorkflowSteps(clonedSteps);
        }

        Map<Long, Long> transitionIdMap = new HashMap<>();
        if (!sourceTransitions.isEmpty()) {
            List<WorkflowEntity.WorkflowTransitionEntity> clonedTransitions = sourceTransitions.stream()
                    .map(transition -> WorkflowEntity.WorkflowTransitionEntity.builder()
                            .tenantId(tenantId)
                            .workflowId(savedWorkflow.getId())
                            .name(transition.getName())
                            .fromStatusId(requireMappedId(statusIdMap, transition.getFromStatusId(), "status"))
                            .toStatusId(requireMappedId(statusIdMap, transition.getToStatusId(), "status"))
                            .sequence(transition.getSequence())
                            .build())
                    .collect(Collectors.toList());

            List<WorkflowEntity.WorkflowTransitionEntity> savedTransitions = workflowTransitionPort
                    .createWorkflowTransitions(clonedTransitions);
            for (int i = 0; i < sourceTransitions.size(); i++) {
                transitionIdMap.put(sourceTransitions.get(i).getId(), savedTransitions.get(i).getId());
            }

            List<WorkflowTransitionRuleEntity> clonedRules = new ArrayList<>();
            for (WorkflowEntity.WorkflowTransitionEntity sourceTransition : sourceTransitions) {
                List<WorkflowTransitionRuleEntity> sourceRules = workflowTransitionRulePort
                        .getWorkflowTransitionRulesByTransitionIdIncludingSystem(sourceTransition.getId(), tenantId);
                Long targetTransitionId = requireMappedId(transitionIdMap, sourceTransition.getId(), "transition");
                sourceRules.stream()
                        .map(rule -> WorkflowTransitionRuleEntity.builder()
                                .tenantId(tenantId)
                                .transitionId(targetTransitionId)
                                .ruleStage(rule.getRuleStage())
                                .ruleKey(rule.getRuleKey())
                                .configJson(rule.getConfigJson())
                                .sequence(rule.getSequence())
                                .isEnabled(rule.getIsEnabled())
                                .build())
                        .forEach(clonedRules::add);
            }

            if (!clonedRules.isEmpty()) {
                workflowTransitionRulePort.createWorkflowTransitionRules(clonedRules);
            }
        }

        log.info("Created {} WORKFLOW clone: source={} -> target={} (tenantId={})",
                cloneMode, source.getId(), savedWorkflow.getId(), tenantId);
        return savedWorkflow.getId();
    }

    private Long materializeStatusForTenant(Long sourceStatusId, Long tenantId, Long userId) {
        StatusEntity source = statusPort
                .getStatusByIdIncludingSystem(sourceStatusId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Status not found for source id=" + sourceStatusId));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Status source is not tenant/system scoped: " + sourceStatusId);
        }

        Optional<StatusEntity> existing = statusPort.getStatusByStatusKey(tenantId, source.getStatusKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        long now = System.currentTimeMillis();
        StatusEntity clone = StatusEntity.builder()
                .tenantId(tenantId)
                .statusKey(source.getStatusKey())
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .categoryId(materializeStatusCategoryForTenant(source.getCategoryId(), tenantId, userId))
                .isSystem(false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        StatusEntity saved = statusPort.createStatus(clone);
        log.info("Materialized STATUS source={} -> tenant={} for tenantId={}",
                sourceStatusId, saved.getId(), tenantId);
        return saved.getId();
    }

    private Long materializeStatusCategoryForTenant(Long sourceCategoryId, Long tenantId, Long userId) {
        if (sourceCategoryId == null) {
            return null;
        }

        StatusCategoryEntity source = statusCategoryPort
                .getStatusCategoryByIdIncludingSystem(sourceCategoryId, tenantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Status category not found for source id=" + sourceCategoryId));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Status category source is not tenant/system scoped: " + sourceCategoryId);
        }

        Optional<StatusCategoryEntity> existing = statusCategoryPort
                .getStatusCategoryByKey(tenantId, source.getKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        long now = System.currentTimeMillis();
        StatusCategoryEntity clone = StatusCategoryEntity.builder()
                .tenantId(tenantId)
                .name(source.getName())
                .key(source.getKey())
                .color(source.getColor())
                .isSystem(false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        StatusCategoryEntity saved = statusCategoryPort.createStatusCategory(clone);
        log.info("Materialized STATUS_CATEGORY source={} -> tenant={} for tenantId={}",
                sourceCategoryId, saved.getId(), tenantId);
        return saved.getId();
    }

    private String derivePriorityKey(PriorityEntity source, Long sourcePriorityId) {
        String priorityKey = source.getPriorityKey();
        if (priorityKey == null || priorityKey.isBlank()) {
            priorityKey = normalizePriorityKey(source.getName());
        }
        if (priorityKey == null || priorityKey.isBlank()) {
            priorityKey = "priority_" + sourcePriorityId;
        }
        return priorityKey;
    }

    private String normalizePriorityKey(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        return normalized.isBlank() ? null : normalized;
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId, String entityName) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new AppException(
                    ErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing " + entityName + " mapping for source id=" + sourceId);
        }
        return mappedId;
    }

    private void upsertTenantSchemeMapping(Optional<TenantSchemeMappingEntity> existing,
                                           Long tenantId,
                                           Long userId,
                                           SchemeType schemeType,
                                           Long sourceSchemeId,
                                           Long tenantSchemeId) {
        long now = System.currentTimeMillis();

        TenantSchemeMappingEntity mapping = existing.orElseGet(() ->
                TenantSchemeMappingEntity.builder()
                        .tenantId(tenantId)
                        .schemeType(schemeType)
                        .sourceSchemeId(sourceSchemeId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build()
        );

        mapping.setTenantSchemeId(tenantSchemeId);
        mapping.setUpdatedAt(now);
        mapping.setUpdatedBy(userId);

        tenantSchemeMappingPort.saveMapping(mapping);
    }

    private void upsertTenantWorkflowMapping(Optional<TenantWorkflowMappingEntity> existing,
                                             Long tenantId,
                                             Long userId,
                                             Long sourceWorkflowId,
                                             Long tenantWorkflowId) {
        long now = System.currentTimeMillis();

        TenantWorkflowMappingEntity mapping = existing.orElseGet(() ->
                TenantWorkflowMappingEntity.builder()
                        .tenantId(tenantId)
                        .sourceWorkflowId(sourceWorkflowId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build()
        );

        mapping.setTenantWorkflowId(tenantWorkflowId);
        mapping.setUpdatedAt(now);
        mapping.setUpdatedBy(userId);

        tenantWorkflowMappingPort.saveMapping(mapping);
    }

    private String buildSchemeCloneName(String sourceName, Long sourceSchemeId, String cloneMode) {
        String safeName = (sourceName == null || sourceName.isBlank()) ? "Scheme" : sourceName;
        String mode = (cloneMode == null || cloneMode.isBlank()) ? "SHARED" : cloneMode;
        String candidate = "[" + mode + "][SRC:" + sourceSchemeId + "] " + safeName;
        if (candidate.length() <= 255) {
            return candidate;
        }
        return candidate.substring(0, 255);
    }

    private String buildSchemeCloneDescription(String sourceDescription, Long sourceSchemeId, String cloneMode) {
        String mode = (cloneMode == null || cloneMode.isBlank()) ? "shared" : cloneMode.toLowerCase(Locale.ROOT);
        String prefix = "Tenant " + mode + " clone from source scheme " + sourceSchemeId;
        if (sourceDescription == null || sourceDescription.isBlank()) {
            return prefix;
        }
        return prefix + " | " + sourceDescription;
    }

    private void stubProvision(String typeName, SchemeType type,
                                  Map<SchemeType, Long> resolvedSources, ProjectEntity project) {
        Long sourceId = resolvedSources.get(type);
        if (sourceId != null) {
            log.warn("Scheme type {} has source ID {} but shared association provisioning is not implemented yet. " +
                    "Skipping provisioning for project key={}. " +
                    "The project's {} binding will remain null.",
                    typeName, sourceId, project.getKey(), typeName);
        }
    }
}
