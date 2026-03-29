/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issyetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.port.IProjectIssueCounterPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.LexorankUtils;

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
        workItem.setCreatedBy(userId);
        workItem.setUpdatedBy(userId);
        workItem.setCreatedAt(now);
        workItem.setUpdatedAt(now);

        WorkItemEntity saved = workItemWritePort.saveWorkItem(workItem);
        log.info("Created work item id={}, key={}, projectId={}", saved.getId(), saved.getKey(), saved.getProjectId());
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
