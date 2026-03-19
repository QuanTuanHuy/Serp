/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.entity.PriorityEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningResult;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.entity.StatusEntity;
import serp.project.pmcore.domain.entity.TenantSchemeDefaultEntity;
import serp.project.pmcore.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.entity.TenantWorkflowMappingEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.AppException;
import serp.project.pmcore.domain.exception.ErrorCode;
import serp.project.pmcore.domain.port.store.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.INotificationSchemePort;
import serp.project.pmcore.domain.port.store.IPermissionSchemePort;
import serp.project.pmcore.domain.port.store.IPriorityPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IStatusCategoryPort;
import serp.project.pmcore.domain.port.store.IStatusPort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.port.store.ITenantSchemeDefaultPort;
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
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

/**
 * Handles scheme provisioning for project creation.
 *
 * Current phase:
 * - resolve source schemes by precedence: explicit override -> blueprint default -> tenant default/shared default
 * - keep create-project orchestration routed through a typed provisioning contract
 * - materialize supported system-owned sources to tenant scope for ISSUE_TYPE/PRIORITY/WORKFLOW
 * - enforce tenant-owned bindings or pre-materialized mapping reuse for other scheme families
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeProvisioningService implements ISchemeProvisioningService {

    private static final Long SYSTEM_TENANT_ID = 0L;

    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    private final ITenantSchemeDefaultPort tenantSchemeDefaultPort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IPermissionSchemePort permissionSchemePort;
    private final INotificationSchemePort notificationSchemePort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
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
    public ProjectProvisioningResult provisionProjectSchemes(ProjectEntity project,
                                                             ProjectProvisioningRequest request) {
        Map<SchemeType, Long> requestedSources = request.getRequestedSchemeBindings() == null
                ? Collections.emptyMap()
                : request.getRequestedSchemeBindings().toSchemeMap();
        Map<SchemeType, Long> blueprintDefaults = loadBlueprintDefaults(request.getBlueprintId(), request.getTenantId());
        Map<SchemeType, Long> tenantDefaults = loadTenantDefaults(request.getTenantId());
        Map<SchemeType, Long> resolvedSources = resolveTemplateSources(requestedSources, blueprintDefaults, tenantDefaults);
        Map<SchemeType, Long> effectiveBindings = provisionEffectiveBindings(
                project,
                resolvedSources,
                request.getTenantId(),
                request.getUserId(),
                request.getEffectiveProvisioningMode()
        );

        return ProjectProvisioningResult.builder()
                .resolvedSourceBindings(ProjectSchemeBindings.fromSchemeMap(resolvedSources))
                .effectiveBindings(ProjectSchemeBindings.fromSchemeMap(effectiveBindings))
                .build();
    }

    private Map<SchemeType, Long> provisionEffectiveBindings(ProjectEntity project,
                                                             Map<SchemeType, Long> resolvedSources,
                                                             Long tenantId,
                                                             Long userId,
                                                             ProvisioningMode provisioningMode) {
        if (ProvisioningMode.CLONE_FROM_SHARED.equals(provisioningMode)) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_PROVISIONING_MODE,
                    "CLONE_FROM_SHARED is reserved for future project rebinding and is not supported by create project yet."
            );
        }

        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);

        Long issueTypeSourceId = requireSourceSchemeId(resolvedSources, SchemeType.ISSUE_TYPE);
        Long workflowSourceId = requireSourceSchemeId(resolvedSources, SchemeType.WORKFLOW);
        Long fieldConfigSourceId = requireSourceSchemeId(resolvedSources, SchemeType.FIELD_CONFIG);
        Long screenSourceId = requireSourceSchemeId(resolvedSources, SchemeType.SCREEN);
        Long permissionSourceId = requireSourceSchemeId(resolvedSources, SchemeType.PERMISSION);
        Long notificationSourceId = requireSourceSchemeId(resolvedSources, SchemeType.NOTIFICATION);
        Long prioritySourceId = requireSourceSchemeId(resolvedSources, SchemeType.PRIORITY);
        Long issueSecuritySourceId = requireSourceSchemeId(resolvedSources, SchemeType.ISSUE_SECURITY);

        if (ProvisioningMode.TEMPLATE_DEFAULT.equals(provisioningMode)) {
            effectiveBindings.put(SchemeType.ISSUE_TYPE,
                    resolveClonedSchemeBinding(SchemeType.ISSUE_TYPE, issueTypeSourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.WORKFLOW,
                    resolveClonedSchemeBinding(SchemeType.WORKFLOW, workflowSourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.FIELD_CONFIG,
                    resolveFieldConfigSchemeBinding(fieldConfigSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.SCREEN,
                    resolveIssueTypeScreenSchemeBinding(screenSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.PERMISSION,
                    resolvePermissionSchemeBinding(permissionSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.NOTIFICATION,
                    resolveNotificationSchemeBinding(notificationSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.PRIORITY,
                    resolveSharedSchemeBinding(SchemeType.PRIORITY, prioritySourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.ISSUE_SECURITY,
                    resolveIssueSecuritySchemeBinding(issueSecuritySourceId, tenantId, provisioningMode));
        } else {
            effectiveBindings.put(SchemeType.ISSUE_TYPE,
                    resolveSharedSchemeBinding(SchemeType.ISSUE_TYPE, issueTypeSourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.WORKFLOW,
                    resolveSharedSchemeBinding(SchemeType.WORKFLOW, workflowSourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.FIELD_CONFIG,
                    resolveFieldConfigSchemeBinding(fieldConfigSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.SCREEN,
                    resolveIssueTypeScreenSchemeBinding(screenSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.PERMISSION,
                    resolvePermissionSchemeBinding(permissionSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.NOTIFICATION,
                    resolveNotificationSchemeBinding(notificationSourceId, tenantId, provisioningMode));
            effectiveBindings.put(SchemeType.PRIORITY,
                    resolveSharedSchemeBinding(SchemeType.PRIORITY, prioritySourceId, tenantId, userId));
            effectiveBindings.put(SchemeType.ISSUE_SECURITY,
                    resolveIssueSecuritySchemeBinding(issueSecuritySourceId, tenantId, provisioningMode));
        }

        log.info("Provisioned schemes for project key={} mode={} resolvedSources={} effectiveBindings={}",
                project.getKey(), provisioningMode, resolvedSources, effectiveBindings);
        return effectiveBindings;
    }

    private Long resolveSharedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId) {
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

    private Long resolveClonedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId) {
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

        Map<SchemeType, BlueprintSchemeDefaultEntity> preferredDefaults = new EnumMap<>(SchemeType.class);
        for (BlueprintSchemeDefaultEntity candidate : defaults) {
            if (candidate.getSchemeType() == null || candidate.getSchemeId() == null) {
                continue;
            }

            BlueprintSchemeDefaultEntity existing = preferredDefaults.get(candidate.getSchemeType());
            if (shouldReplaceDefault(existing == null ? null : existing.getTenantId(), candidate.getTenantId(), tenantId)) {
                preferredDefaults.put(candidate.getSchemeType(), candidate);
            }
        }

        Map<SchemeType, Long> resolvedDefaults = new EnumMap<>(SchemeType.class);
        for (Map.Entry<SchemeType, BlueprintSchemeDefaultEntity> entry : preferredDefaults.entrySet()) {
            resolvedDefaults.put(entry.getKey(), entry.getValue().getSchemeId());
        }

        return resolvedDefaults;
    }

    private Map<SchemeType, Long> loadTenantDefaults(Long tenantId) {
        List<TenantSchemeDefaultEntity> defaults = tenantSchemeDefaultPort.getDefaultsByTenantIdIncludingSystem(tenantId);
        Map<SchemeType, TenantSchemeDefaultEntity> preferredDefaults = new EnumMap<>(SchemeType.class);

        for (TenantSchemeDefaultEntity candidate : defaults) {
            if (candidate.getSchemeType() == null || candidate.getSchemeId() == null) {
                continue;
            }

            TenantSchemeDefaultEntity existing = preferredDefaults.get(candidate.getSchemeType());
            if (shouldReplaceDefault(existing == null ? null : existing.getTenantId(), candidate.getTenantId(), tenantId)) {
                preferredDefaults.put(candidate.getSchemeType(), candidate);
            }
        }

        Map<SchemeType, Long> resolvedDefaults = new EnumMap<>(SchemeType.class);
        for (Map.Entry<SchemeType, TenantSchemeDefaultEntity> entry : preferredDefaults.entrySet()) {
            resolvedDefaults.put(entry.getKey(), entry.getValue().getSchemeId());
        }

        return resolvedDefaults;
    }

    private boolean shouldReplaceDefault(Long existingTenantId, Long candidateTenantId, Long tenantId) {
        if (existingTenantId == null) {
            return true;
        }

        return tenantId.equals(candidateTenantId) && !tenantId.equals(existingTenantId);
    }

    private Map<SchemeType, Long> resolveTemplateSources(Map<SchemeType, Long> overrides,
                                                         Map<SchemeType, Long> blueprintDefaults,
                                                         Map<SchemeType, Long> tenantDefaults) {
        Map<SchemeType, Long> resolved = new EnumMap<>(SchemeType.class);

        for (SchemeType type : SchemeType.values()) {
            if (overrides != null && overrides.containsKey(type)) {
                resolved.put(type, overrides.get(type));
                continue;
            }

            if (blueprintDefaults.containsKey(type)) {
                resolved.put(type, blueprintDefaults.get(type));
                continue;
            }

            if (tenantDefaults.containsKey(type)) {
                resolved.put(type, tenantDefaults.get(type));
            }
        }

        return resolved;
    }

    private Long requireSourceSchemeId(Map<SchemeType, Long> resolvedSources, SchemeType type) {
        Long sourceId = resolvedSources.get(type);
        if (sourceId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing source scheme for " + type
                            + ". Provide an explicit override, blueprint default, or tenant default/shared default."
            );
        }
        return sourceId;
    }

    private Long resolveFieldConfigSchemeBinding(Long sourceSchemeId, Long tenantId, ProvisioningMode provisioningMode) {
        FieldConfigSchemeEntity source = fieldConfigSchemePort
                .getFieldConfigSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return resolveTenantOwnedBindingUntilImplemented(
                "FIELD_CONFIG",
                SchemeType.FIELD_CONFIG,
                source.getId(),
                source.getTenantId(),
                tenantId,
                provisioningMode,
                tenantSchemeId -> fieldConfigSchemePort.getFieldConfigSchemeById(tenantSchemeId, tenantId).isPresent()
        );
    }

    private Long resolveIssueTypeScreenSchemeBinding(Long sourceSchemeId, Long tenantId, ProvisioningMode provisioningMode) {
        IssueTypeScreenSchemeEntity source = issueTypeScreenSchemePort
                .getIssueTypeScreenSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return resolveTenantOwnedBindingUntilImplemented(
                "SCREEN",
                SchemeType.SCREEN,
                source.getId(),
                source.getTenantId(),
                tenantId,
                provisioningMode,
                tenantSchemeId -> issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(tenantSchemeId, tenantId).isPresent()
        );
    }

    private Long resolvePermissionSchemeBinding(Long sourceSchemeId, Long tenantId, ProvisioningMode provisioningMode) {
        PermissionSchemeEntity source = permissionSchemePort
                .getPermissionSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return resolveTenantOwnedBindingUntilImplemented(
                "PERMISSION",
                SchemeType.PERMISSION,
                source.getId(),
                source.getTenantId(),
                tenantId,
                provisioningMode,
                tenantSchemeId -> permissionSchemePort.getPermissionSchemeById(tenantSchemeId, tenantId).isPresent()
        );
    }

    private Long resolveNotificationSchemeBinding(Long sourceSchemeId, Long tenantId, ProvisioningMode provisioningMode) {
        NotificationSchemeEntity source = notificationSchemePort
                .getNotificationSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return resolveTenantOwnedBindingUntilImplemented(
                "NOTIFICATION",
                SchemeType.NOTIFICATION,
                source.getId(),
                source.getTenantId(),
                tenantId,
                provisioningMode,
                tenantSchemeId -> notificationSchemePort.getNotificationSchemeById(tenantSchemeId, tenantId).isPresent()
        );
    }

    private Long resolveIssueSecuritySchemeBinding(Long sourceSchemeId, Long tenantId, ProvisioningMode provisioningMode) {
        IssueSecuritySchemeEntity source = issueSecuritySchemePort
                .getIssueSecuritySchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        return resolveTenantOwnedBindingUntilImplemented(
                "ISSUE_SECURITY",
                SchemeType.ISSUE_SECURITY,
                source.getId(),
                source.getTenantId(),
                tenantId,
                provisioningMode,
                tenantSchemeId -> issueSecuritySchemePort.getIssueSecuritySchemeById(tenantSchemeId, tenantId).isPresent()
        );
    }

    private Long resolveTenantOwnedBindingUntilImplemented(String schemeFamily,
                                                           SchemeType schemeType,
                                                           Long sourceSchemeId,
                                                           Long sourceTenantId,
                                                           Long tenantId,
                                                           ProvisioningMode provisioningMode,
                                                           LongPredicate tenantSchemeExists) {
        if (tenantId.equals(sourceTenantId)) {
            log.warn("Phase 2 fallback binds tenant-owned {} scheme {} directly in mode {}",
                    schemeFamily, sourceSchemeId, provisioningMode);
            return sourceSchemeId;
        }

        if (SYSTEM_TENANT_ID.equals(sourceTenantId)) {
            Optional<TenantSchemeMappingEntity> mapping = tenantSchemeMappingPort
                    .getMapping(tenantId, schemeType, sourceSchemeId);

            if (mapping.isPresent()) {
                Long tenantSchemeId = mapping.get().getTenantSchemeId();
                if (tenantSchemeExists.test(tenantSchemeId)) {
                    log.info("Reusing pre-materialized tenant mapping for {} sourceSchemeId={} tenantSchemeId={}",
                            schemeFamily, sourceSchemeId, tenantSchemeId);
                    return tenantSchemeId;
                }

                log.warn("Stale {} mapping found (tenantId={}, sourceSchemeId={}, mappedSchemeId={})",
                        schemeFamily, tenantId, sourceSchemeId, tenantSchemeId);
            }

            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "System-owned " + schemeFamily + " provisioning is not implemented yet for mode "
                            + provisioningMode + ". Materialize a tenant-owned scheme and tenant mapping first."
            );
        }

        throw new AppException(ErrorCode.SCHEME_NOT_FOUND);
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

}
