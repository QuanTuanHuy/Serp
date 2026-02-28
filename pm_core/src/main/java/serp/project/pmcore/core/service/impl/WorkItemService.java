/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.core.domain.entity.IssueTypeEntity;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IIssueTypePort;
import serp.project.pmcore.core.port.store.IProjectIssueCounterPort;
import serp.project.pmcore.core.port.store.IWorkItemPort;
import serp.project.pmcore.core.service.IWorkItemService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemService implements IWorkItemService {

    private final IWorkItemPort workItemPort;
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

        WorkItemEntity saved = workItemPort.saveWorkItem(workItem);
        log.info("Created work item id={}, key={}, projectId={}", saved.getId(), saved.getKey(), saved.getProjectId());
        return saved;
    }

    @Override
    public WorkItemEntity getWorkItemById(Long id, Long tenantId) {
        return workItemPort.getWorkItemById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_ITEM_NOT_FOUND));
    }

    @Override
    public long getNextIssueNumber(Long projectId, Long tenantId) {
        return projectIssueCounterPort.getNextIssueNo(projectId, tenantId);
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
            throw new AppException(ErrorCode.INVALID_PARENT_HIERARCHY);
        }

        IssueTypeEntity parentIssueType = issueTypePort.getIssueTypeById(parent.getIssueTypeId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        IssueTypeEntity childIssueType = issueTypePort.getIssueTypeById(childIssueTypeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (childIssueType.getHierarchyLevel() >= parentIssueType.getHierarchyLevel()) {
            throw new AppException(ErrorCode.INVALID_PARENT_HIERARCHY);
        }
    }
}
