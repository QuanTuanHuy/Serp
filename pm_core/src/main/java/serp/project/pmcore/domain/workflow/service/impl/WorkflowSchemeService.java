/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowSchemeService implements IWorkflowSchemeService {

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IProjectReadPort projectReadPort;

    @Override
    public WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme, Long tenantId, Long userId) {
        String name = TextNormalizationUtils.normalizeRequiredText(scheme.getName(), "name", 255);
        if (workflowSchemePort.existsByName(tenantId, name)) {
            log.warn("Workflow scheme name already exists: tenantId={}, name={}", tenantId, name);
            throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_SCHEME_NAME_ALREADY_EXISTS);
        }

        Long defaultWorkflowId = requireVisibleActiveWorkflowId(scheme.getDefaultWorkflowId(), tenantId, "defaultWorkflowId");
        long now = System.currentTimeMillis();

        scheme.setTenantId(tenantId);
        scheme.setName(name);
        scheme.setDescription(TextNormalizationUtils.normalizeOptionalText(scheme.getDescription(), 2000, "description"));
        scheme.setDefaultWorkflowId(defaultWorkflowId);
        scheme.setDeletedAt(null);
        scheme.setItems(List.of());
        scheme.applyCreate(userId, now);
        return workflowSchemePort.createWorkflowScheme(scheme);
    }

    @Override
    public WorkflowSchemeEntity getWorkflowSchemeById(Long workflowSchemeId, Long tenantId) {
        return workflowSchemePort.getWorkflowSchemeById(workflowSchemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflowScheme(workflowSchemeId));
    }

    @Override
    public WorkflowSchemeEntity getVisibleWorkflowSchemeById(Long workflowSchemeId, Long tenantId) {
        return workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(workflowSchemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflowScheme(workflowSchemeId));
    }

    @Override
    public WorkflowSchemeEntity getVisibleWorkflowSchemeDetailById(Long workflowSchemeId, Long tenantId) {
        WorkflowSchemeEntity scheme = getVisibleWorkflowSchemeById(workflowSchemeId, tenantId);
        scheme.setItems(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeIdIncludingSystem(workflowSchemeId, tenantId));
        return scheme;
    }

    @Override
    public PageResult<WorkflowSchemeEntity> listVisibleWorkflowSchemes(Long tenantId, WorkflowSchemeListCriteria criteria) {
        return workflowSchemePort.listWorkflowSchemesIncludingSystem(tenantId, criteria);
    }

    @Override
    public WorkflowSchemeEntity updateWorkflowScheme(Long workflowSchemeId,
                                                     WorkflowSchemeUpdateData data,
                                                     Long tenantId,
                                                     Long userId) {
        WorkflowSchemeEntity existing = getWorkflowSchemeById(workflowSchemeId, tenantId);

        if (data.nameProvided()) {
            String newName = TextNormalizationUtils.normalizeRequiredText(data.name(), "name", 255);
            if (!newName.equalsIgnoreCase(existing.getName()) && workflowSchemePort.existsByName(tenantId, newName)) {
                throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_SCHEME_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(TextNormalizationUtils.normalizeOptionalText(data.description(), 2000, "description"));
        }

        if (data.defaultWorkflowIdProvided()) {
            Long defaultWorkflowId = requireVisibleActiveWorkflowId(data.defaultWorkflowId(), tenantId, "defaultWorkflowId");
            existing.setDefaultWorkflowId(defaultWorkflowId);
        }

        validateSchemeCoverageForBoundProjects(existing, tenantId);

        existing.applyUpdate(userId, System.currentTimeMillis());
        workflowSchemePort.updateWorkflowScheme(existing);
        return existing;
    }

    @Override
    public WorkflowSchemeEntity deleteWorkflowScheme(Long workflowSchemeId, Long tenantId, Long userId) {
        WorkflowSchemeEntity existing = getWorkflowSchemeById(workflowSchemeId, tenantId);
        if (projectReadPort.existsActiveProjectByWorkflowSchemeId(workflowSchemeId, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_SCHEME_BOUND_TO_PROJECT);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        workflowSchemePort.updateWorkflowScheme(existing);
        workflowSchemeItemPort.deleteWorkflowSchemeItemsBySchemeId(workflowSchemeId, tenantId);
        existing.setItems(List.of());
        return existing;
    }

    @Override
    public WorkflowSchemeEntity replaceWorkflowSchemeItems(Long workflowSchemeId,
                                                           List<WorkflowSchemeItemReplacement> items,
                                                           Long tenantId,
                                                           Long userId) {
        WorkflowSchemeEntity existing = getWorkflowSchemeById(workflowSchemeId, tenantId);
        List<WorkflowSchemeItemReplacement> normalizedItems = normalizeItems(items);

        requireVisibleIssueTypes(normalizedItems, tenantId);
        requireVisibleActiveWorkflows(normalizedItems, tenantId);

        long now = System.currentTimeMillis();
        workflowSchemeItemPort.deleteWorkflowSchemeItemsBySchemeId(workflowSchemeId, tenantId);

        List<WorkflowSchemeItemEntity> replacementItems = new ArrayList<>(normalizedItems.size());
        for (WorkflowSchemeItemReplacement item : normalizedItems) {
            WorkflowSchemeItemEntity replacement = WorkflowSchemeItemEntity.builder()
                    .tenantId(tenantId)
                    .schemeId(workflowSchemeId)
                    .issueTypeId(item.issueTypeId())
                    .workflowId(item.workflowId())
                    .createdAt(now)
                    .createdBy(userId)
                    .updatedAt(now)
                    .updatedBy(userId)
                    .build();
            replacementItems.add(replacement);
        }

        List<WorkflowSchemeItemEntity> savedItems = workflowSchemeItemPort.createWorkflowSchemeItems(replacementItems);
        existing.setItems(savedItems);
        validateSchemeCoverageForBoundProjects(existing, tenantId);

        existing.applyUpdate(userId, now);
        workflowSchemePort.updateWorkflowScheme(existing);
        return existing;
    }

    @Override
    public Long resolveWorkflowId(Long workflowSchemeId, Long issueTypeId, Long tenantId) {
        WorkflowSchemeEntity scheme = workflowSchemePort
                .getWorkflowSchemeById(workflowSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        Long workflowId = workflowSchemeItemPort
                .getWorkflowSchemeItemsBySchemeId(workflowSchemeId, tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .findFirst()
                .orElse(scheme.getDefaultWorkflowId());
        if (workflowId == null) {
            throw new AppException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        return workflowId;
    }

    private Long requireVisibleActiveWorkflowId(Long workflowId, Long tenantId, String fieldName) {
        if (workflowId == null || workflowId <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        WorkflowEntity workflow = workflowPort.getWorkflowByIdIncludingSystem(workflowId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflowId));
        requireWorkflowActive(workflow, tenantId);
        return workflow.getId();
    }

    private Map<Long, IssueTypeEntity> requireVisibleIssueTypes(List<WorkflowSchemeItemReplacement> items, Long tenantId) {
        List<Long> issueTypeIds = items.stream()
                .map(WorkflowSchemeItemReplacement::issueTypeId)
                .distinct()
                .toList();
        List<IssueTypeEntity> visibleIssueTypes = issueTypePort.getIssueTypesByIdsIncludingSystem(issueTypeIds, tenantId);
        Map<Long, IssueTypeEntity> issueTypesById = visibleIssueTypes.stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, Function.identity()));

        for (Long issueTypeId : issueTypeIds) {
            if (!issueTypesById.containsKey(issueTypeId)) {
                throw ResourceNotFoundException.issueType(issueTypeId);
            }
        }
        return issueTypesById;
    }

    private Map<Long, WorkflowEntity> requireVisibleActiveWorkflows(List<WorkflowSchemeItemReplacement> items, Long tenantId) {
        Map<Long, WorkflowEntity> workflowsById = new LinkedHashMap<>();
        for (WorkflowSchemeItemReplacement item : items) {
            WorkflowEntity workflow = workflowPort.getWorkflowByIdIncludingSystem(item.workflowId(), tenantId)
                    .orElseThrow(() -> ResourceNotFoundException.workflow(item.workflowId()));
            requireWorkflowActive(workflow, tenantId);
            workflowsById.put(workflow.getId(), workflow);
        }
        return workflowsById;
    }

    private void requireWorkflowActive(WorkflowEntity workflow, Long tenantId) {
        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_NOT_ACTIVE);
        }

        WorkflowVersionEntity version = workflowVersionPort
                .getWorkflowVersionByIdIncludingSystem(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflow.getId()));
        if (!version.isActive()) {
            throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_NOT_ACTIVE);
        }
    }

    private List<WorkflowSchemeItemReplacement> normalizeItems(List<WorkflowSchemeItemReplacement> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }

        List<WorkflowSchemeItemReplacement> normalized = new ArrayList<>(items.size());
        Set<Long> issueTypeIds = new LinkedHashSet<>();
        for (WorkflowSchemeItemReplacement item : items) {
            if (item == null) {
                throw new IllegalArgumentException("items must not contain null");
            }
            if (item.issueTypeId() == null || item.issueTypeId() <= 0) {
                throw new IllegalArgumentException("issueTypeId must be greater than 0");
            }
            if (item.workflowId() == null || item.workflowId() <= 0) {
                throw new IllegalArgumentException("workflowId must be greater than 0");
            }
            if (!issueTypeIds.add(item.issueTypeId())) {
                throw new IllegalArgumentException("items must not contain duplicate issueTypeId values");
            }
            normalized.add(item);
        }
        return normalized;
    }

    private void validateSchemeCoverageForBoundProjects(WorkflowSchemeEntity scheme, Long tenantId) {
        List<Long> boundProjectIds = projectReadPort.getActiveProjectIdsByWorkflowSchemeId(scheme.getId(), tenantId);
        if (boundProjectIds.isEmpty()) {
            return;
        }

        for (Long projectId : boundProjectIds) {
            validateSchemeCoverageForProject(scheme, projectId, tenantId);
        }
    }

    private void validateSchemeCoverageForProject(WorkflowSchemeEntity scheme, Long projectId, Long tenantId) {
        IssueTypeSchemeEntity issueTypeScheme = projectReadPort.getProjectById(projectId, tenantId)
                .flatMap(project -> issueTypeSchemePort.getIssueTypeSchemeById(project.getIssueTypeSchemeId(), tenantId))
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                        "Issue type scheme not found for projectId=" + projectId
                ));

        List<IssueTypeSchemeItemEntity> issueTypeItems = issueTypeSchemeItemPort
                .getIssueTypeSchemeItemsBySchemeId(issueTypeScheme.getId(), tenantId);
        Set<Long> issueTypeIds = issueTypeItems.stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (issueTypeScheme.getDefaultIssueTypeId() != null) {
            issueTypeIds.add(issueTypeScheme.getDefaultIssueTypeId());
        }

        Map<Long, Long> mappedWorkflows = (scheme.getItems() == null ? List.<WorkflowSchemeItemEntity>of() : scheme.getItems())
                .stream()
                .collect(Collectors.toMap(WorkflowSchemeItemEntity::getIssueTypeId,
                        WorkflowSchemeItemEntity::getWorkflowId,
                        (left, right) -> right,
                        LinkedHashMap::new));

        for (Long issueTypeId : issueTypeIds) {
            Long workflowId = mappedWorkflows.get(issueTypeId);
            if (workflowId == null) {
                workflowId = scheme.getDefaultWorkflowId();
            }
            if (workflowId == null) {
                throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING,
                        "Workflow scheme does not cover issue type id=" + issueTypeId + " for projectId=" + projectId
                );
            }
            Long effectiveWorkflowId = workflowId;
            WorkflowEntity workflow = workflowPort.getWorkflowByIdIncludingSystem(effectiveWorkflowId, tenantId)
                    .orElseThrow(() -> ResourceNotFoundException.workflow(effectiveWorkflowId));
            requireWorkflowActive(workflow, tenantId);
        }
    }
}
