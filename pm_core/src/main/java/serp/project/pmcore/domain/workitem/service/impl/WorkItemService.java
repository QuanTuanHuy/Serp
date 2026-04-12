/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.port.IProjectIssueCounterPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.LexorankUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemService implements IWorkItemService {

    private final IWorkItemWritePort workItemWritePort;
    private final IWorkItemReadPort workItemReadPort;
    private final IProjectIssueCounterPort projectIssueCounterPort;
    private final IIssueTypePort issueTypePort;

    @Override
    public WorkItemEntity createWorkItem(WorkItemEntity workItem, Long tenantId, Long userId) {
        long now = System.currentTimeMillis();
        workItem.setTenantId(tenantId);
        workItem.applyCreate(userId, now);

        WorkItemEntity saved = workItemWritePort.saveWorkItem(workItem);
        log.info("Created work item id={}, key={}, projectId={}", saved.getId(), saved.getKey(), saved.getProjectId());
        return saved;
    }

    @Override
    public WorkItemEntity updateWorkItem(WorkItemEntity workItem, Long userId) {
        workItem.applyUpdate(userId, System.currentTimeMillis());
        WorkItemEntity saved = workItemWritePort.saveWorkItem(workItem);
        log.info("Updated work item id={}, key={}, projectId={}",
                saved.getId(), saved.getKey(), saved.getProjectId());
        return saved;
    }

    @Override
    public WorkItemEntity getWorkItemById(Long id, Long tenantId) {
        return workItemReadPort.getWorkItemById(id, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workItem(id));
    }

    @Override
    public long getNextIssueNumber(Long projectId, Long tenantId) {
        return projectIssueCounterPort.getNextIssueNo(projectId, tenantId);
    }

    @Override
    public String getNextRank(Long projectId, Long tenantId) {
        return workItemReadPort.getLastRankByProjectId(projectId, tenantId)
                .map(LexorankUtils::generateRankAfter)
                .orElseGet(LexorankUtils::generateInitialRank);
    }

    @Override
    public void validateParentHierarchy(Long parentId, Long childIssueTypeId,
            Long projectId, Long tenantId) {
        if (parentId == null || childIssueTypeId == null || projectId == null) {
            log.warn("Parent ID or Child Issue Type ID or Project ID is null, skipping parent hierarchy validation");
            return;
        }

        WorkItemEntity parent = getWorkItemById(parentId, tenantId);
        if (!projectId.equals(parent.getProjectId())) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PARENT_NOT_IN_SAME_PROJECT,
                    "Parent work item must belong to the same project: parentId=" + parentId
                            + ", parentProjectId=" + parent.getProjectId()
                            + ", projectId=" + projectId
            );
        }

        IssueTypeEntity parentIssueType = issueTypePort.getIssueTypeById(parent.getIssueTypeId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(parent.getIssueTypeId()));
        IssueTypeEntity childIssueType = issueTypePort.getIssueTypeById(childIssueTypeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(childIssueTypeId));

        int parentLevel = safeHierarchyLevel(parentIssueType);
        int childLevel = safeHierarchyLevel(childIssueType);

        if (childLevel == 0) {
            if (parentLevel != 1) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.INVALID_PARENT_HIERARCHY,
                        "Subtask must have a standard parent: childIssueTypeId=" + childIssueTypeId
                                + ", parentIssueTypeId=" + parent.getIssueTypeId()
                                + ", parentLevel=" + parentLevel
                );
            }
            return;
        }

        if (childLevel == 1) {
            if (parentLevel < 2) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.INVALID_PARENT_HIERARCHY,
                        "Standard issue parent must be epic or higher: childIssueTypeId=" + childIssueTypeId
                                + ", parentIssueTypeId=" + parent.getIssueTypeId()
                                + ", parentLevel=" + parentLevel
                );
            }
            return;
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.INVALID_PARENT_HIERARCHY,
                "Issue types with hierarchy level >= 2 cannot set parent: childIssueTypeId=" + childIssueTypeId
                        + ", childLevel=" + childLevel
        );
    }

    @Override
    public WorkItemDeleteExecutionResult softDeleteWorkItem(Long rootWorkItemId,
                                                            Long projectId,
                                                            Long tenantId,
                                                            Long userId,
                                                            Long deletedAt) {
        WorkItemEntity rootWorkItem = getWorkItemById(rootWorkItemId, tenantId);
        if (!projectId.equals(rootWorkItem.getProjectId())) {
            throw new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }

        LinkedHashSet<Long> scopeIds = new LinkedHashSet<>();
        Map<Long, VisitState> visitStates = new HashMap<>();

        collectDeleteScope(rootWorkItem, projectId, tenantId, visitStates, scopeIds);

        WorkItemDeleteExecutionResult result = workItemWritePort.softDeleteWorkItems(
                projectId,
                tenantId,
                scopeIds,
                userId,
                deletedAt
        );

        log.info("Soft-deleted work item scope: rootId={}, scopeSize={}, deletedWorkItems={}, deletedRelations={}, deletedLinks={}",
                rootWorkItemId,
                scopeIds.size(),
                result.deletedWorkItemCount(),
                result.deletedRelationCount(),
                result.deletedLinkCount());

        return result;
    }

    private void collectDeleteScope(WorkItemEntity current,
                                    Long projectId,
                                    Long tenantId,
                                    Map<Long, VisitState> visitStates,
                                    LinkedHashSet<Long> scopeIds) {
        VisitState state = visitStates.get(current.getId());
        if (state == VisitState.VISITING) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.WORK_ITEM_DELETE_SCOPE_INVALID,
                    "Cycle detected while resolving delete scope: workItemId=" + current.getId()
            );
        }
        if (state == VisitState.VISITED) {
            return;
        }

        visitStates.put(current.getId(), VisitState.VISITING);
        scopeIds.add(current.getId());

        List<WorkItemEntity> children = workItemReadPort.getActiveChildrenByParentId(current.getId(), tenantId);
        for (WorkItemEntity child : children) {
            if (!projectId.equals(child.getProjectId())) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.WORK_ITEM_DELETE_SCOPE_INVALID,
                        "Cross-project child detected while resolving delete scope: parentId=" + current.getId()
                                + ", childId=" + child.getId()
                                + ", childProjectId=" + child.getProjectId()
                                + ", expectedProjectId=" + projectId
                );
            }
            collectDeleteScope(child, projectId, tenantId, visitStates, scopeIds);
        }

        visitStates.put(current.getId(), VisitState.VISITED);
    }

    private enum VisitState {
        VISITING,
        VISITED
    }

    private int safeHierarchyLevel(IssueTypeEntity issueType) {
        if (issueType.getHierarchyLevel() == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue type hierarchy level is missing: issueTypeId=" + issueType.getId()
            );
        }
        return issueType.getHierarchyLevel();
    }
}
