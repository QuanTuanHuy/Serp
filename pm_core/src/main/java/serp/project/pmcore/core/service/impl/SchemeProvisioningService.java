/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.core.domain.entity.*;
import serp.project.pmcore.core.domain.enums.SchemeType;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.*;
import serp.project.pmcore.core.service.ISchemeProvisioningService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles deep-clone provisioning of project-owned scheme bindings.
 *
 * Currently implements real deep clone for:
 * - ISSUE_TYPE scheme (issue_type_schemes + issue_type_scheme_items)
 * - PRIORITY scheme (priority_schemes + priority_scheme_items)
 *
 * All other scheme types (WORKFLOW, FIELD_CONFIG, SCREEN, PERMISSION,
 * ISSUE_SECURITY, NOTIFICATION) are stubbed with log warnings until
 * their infrastructure is built.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeProvisioningService implements ISchemeProvisioningService {

    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;

    @Override
    public void provisionSchemes(ProjectEntity project, Long tenantId, Long userId,
                                 Long blueprintId, Map<String, Long> schemeOverrides) {
        Map<SchemeType, Long> blueprintDefaults = loadBlueprintDefaults(blueprintId, tenantId);

        Map<SchemeType, Long> resolvedSources = resolveTemplateSources(schemeOverrides, blueprintDefaults);

        cloneIssueTypeScheme(project, tenantId, userId, resolvedSources.get(SchemeType.ISSUE_TYPE));
        clonePriorityScheme(project, tenantId, userId, resolvedSources.get(SchemeType.PRIORITY));

        // Stub provisioning for scheme types without infrastructure
        stubProvision("WORKFLOW", SchemeType.WORKFLOW, resolvedSources, project);
        stubProvision("FIELD_CONFIG", SchemeType.FIELD_CONFIG, resolvedSources, project);
        stubProvision("SCREEN", SchemeType.SCREEN, resolvedSources, project);
        stubProvision("PERMISSION", SchemeType.PERMISSION, resolvedSources, project);
        stubProvision("ISSUE_SECURITY", SchemeType.ISSUE_SECURITY, resolvedSources, project);
        stubProvision("NOTIFICATION", SchemeType.NOTIFICATION, resolvedSources, project);
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

    /**
     * Deep-clone an IssueType scheme:
     * 1. Load source scheme + items
     * 2. Create new scheme with cloned name
     * 3. Clone all items with new schemeId
     * 4. Patch default_issue_type_id (same as source — issue types are shared, not cloned)
     * 5. Set cloned scheme ID on project
     */
    private void cloneIssueTypeScheme(ProjectEntity project, Long tenantId, Long userId, Long sourceSchemeId) {
        if (sourceSchemeId == null) {
            log.info("No IssueType scheme source to clone for project key={}", project.getKey());
            return;
        }

        try {
            IssueTypeSchemeEntity source = issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

            List<IssueTypeSchemeItemEntity> sourceItems =
                    issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId);

            IssueTypeSchemeEntity clonedScheme = IssueTypeSchemeEntity.builder()
                    .tenantId(tenantId)
                    .name(project.getKey() + " - " + source.getName())
                    .description("Cloned for project " + project.getKey())
                    .defaultIssueTypeId(source.getDefaultIssueTypeId())
                    .build();
            clonedScheme.setCreatedBy(userId);
            clonedScheme.setUpdatedBy(userId);

            IssueTypeSchemeEntity saved = issueTypeSchemePort.createIssueTypeScheme(clonedScheme);

            if (!sourceItems.isEmpty()) {
                List<IssueTypeSchemeItemEntity> clonedItems = sourceItems.stream()
                        .map(item -> IssueTypeSchemeItemEntity.builder()
                                .tenantId(tenantId)
                                .schemeId(saved.getId())
                                .issueTypeId(item.getIssueTypeId())
                                .sequence(item.getSequence())
                                .build())
                        .collect(Collectors.toList());
                issueTypeSchemeItemPort.createIssueTypeSchemeItems(clonedItems);
            }

            project.setIssueTypeSchemeId(saved.getId());
            log.info("Cloned IssueType scheme: source={} -> cloned={} for project={}",
                    sourceSchemeId, saved.getId(), project.getKey());

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to clone IssueType scheme {} for project {}: {}",
                    sourceSchemeId, project.getKey(), e.getMessage(), e);
            throw new AppException(ErrorCode.SCHEME_PROVISIONING_FAILED);
        }
    }

    /**
     * Deep-clone a Priority scheme:
     * 1. Load source scheme + items
     * 2. Create new scheme with cloned name
     * 3. Clone all items with new schemeId
     * 4. Patch default_priority_id (same as source — priorities are shared, not cloned)
     * 5. Set cloned scheme ID on project
     */
    private void clonePriorityScheme(ProjectEntity project, Long tenantId, Long userId, Long sourceSchemeId) {
        if (sourceSchemeId == null) {
            log.info("No Priority scheme source to clone for project key={}", project.getKey());
            return;
        }

        try {
            PrioritySchemeEntity source = prioritySchemePort.getPrioritySchemeByIdIncludingSystem(sourceSchemeId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

            List<PrioritySchemeItemEntity> sourceItems =
                    prioritySchemeItemPort.getPrioritySchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId);

            PrioritySchemeEntity clonedScheme = PrioritySchemeEntity.builder()
                    .tenantId(tenantId)
                    .name(project.getKey() + " - " + source.getName())
                    .description("Cloned for project " + project.getKey())
                    .defaultPriorityId(source.getDefaultPriorityId())
                    .build();
            clonedScheme.setCreatedBy(userId);
            clonedScheme.setUpdatedBy(userId);

            PrioritySchemeEntity saved = prioritySchemePort.createPriorityScheme(clonedScheme);

            if (!sourceItems.isEmpty()) {
                List<PrioritySchemeItemEntity> clonedItems = sourceItems.stream()
                        .map(item -> PrioritySchemeItemEntity.builder()
                                .tenantId(tenantId)
                                .schemeId(saved.getId())
                                .priorityId(item.getPriorityId())
                                .sequence(item.getSequence())
                                .build())
                        .collect(Collectors.toList());
                prioritySchemeItemPort.createPrioritySchemeItems(clonedItems);
            }

            project.setPrioritySchemeId(saved.getId());
            log.info("Cloned Priority scheme: source={} -> cloned={} for project={}",
                    sourceSchemeId, saved.getId(), project.getKey());

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to clone Priority scheme {} for project {}: {}",
                    sourceSchemeId, project.getKey(), e.getMessage(), e);
            throw new AppException(ErrorCode.SCHEME_PROVISIONING_FAILED);
        }
    }

    /**
     * Stub provisioning for scheme types without infrastructure.
     * Logs a warning and skips — the project's scheme column remains null.
     */
    private void stubProvision(String typeName, SchemeType type,
                                Map<SchemeType, Long> resolvedSources, ProjectEntity project) {
        Long sourceId = resolvedSources.get(type);
        if (sourceId != null) {
            log.warn("Scheme type {} has source ID {} but deep-clone is not yet implemented. " +
                    "Skipping provisioning for project key={}. " +
                    "The project's {} binding will remain null.",
                    typeName, sourceId, project.getKey(), typeName);
        }
    }
}
