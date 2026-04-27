/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.port.IWorklogPort;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;
import serp.project.pmcore.domain.worklog.service.IWorklogService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorklogService implements IWorklogService {

    private final IWorklogPort worklogPort;
    private final IWorkItemWritePort workItemWritePort;

    @Override
    public WorklogEntity createWorklog(WorklogEntity worklog, Long tenantId, Long userId) {
        long now = System.currentTimeMillis();
        worklog.setTenantId(tenantId);
        worklog.applyCreate(userId, now);
        WorklogEntity saved = worklogPort.saveWorklog(worklog);
        log.info("Created worklog id={} workItemId={} tenantId={}",
                saved.getId(), saved.getWorkItemId(), saved.getTenantId());
        return saved;
    }

    @Override
    public WorklogEntity updateWorklog(WorklogEntity worklog, Long userId) {
        worklog.applyUpdate(userId, System.currentTimeMillis());
        WorklogEntity saved = worklogPort.saveWorklog(worklog);
        log.info("Updated worklog id={} workItemId={}", saved.getId(), saved.getWorkItemId());
        return saved;
    }

    @Override
    public WorklogEntity softDeleteWorklog(WorklogEntity worklog, Long userId, Long deletedAt) {
        worklog.setDeletedAt(deletedAt);
        worklog.applyUpdate(userId, deletedAt);
        WorklogEntity saved = worklogPort.saveWorklog(worklog);
        log.info("Soft-deleted worklog id={} workItemId={}", saved.getId(), saved.getWorkItemId());
        return saved;
    }

    @Override
    public WorklogEntity getWorklogById(Long worklogId, Long tenantId) {
        return worklogPort.getWorklogById(worklogId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.worklog(worklogId));
    }

    @Override
    public PageResult<WorklogEntity> listWorklogs(Long tenantId, WorklogListCriteria criteria) {
        return worklogPort.listWorklogs(tenantId, criteria);
    }

    @Override
    public WorkItemEntity refreshWorkItemTimeTracking(WorkItemEntity workItem, Long userId) {
        long totalTimeSpent = worklogPort.sumActiveTimeSpentByWorkItemId(workItem.getId(), workItem.getTenantId());
        workItem.setTimeSpent(totalTimeSpent);
        if (workItem.getTimeOriginalEstimate() == null) {
            workItem.setTimeRemainingEstimate(null);
        } else {
            workItem.setTimeRemainingEstimate(Math.max(workItem.getTimeOriginalEstimate() - totalTimeSpent, 0L));
        }
        workItem.applyUpdate(userId, System.currentTimeMillis());
        WorkItemEntity saved = workItemWritePort.saveWorkItem(workItem);
        log.info("Refreshed work item time tracking: workItemId={} timeSpent={} timeRemainingEstimate={}",
                saved.getId(), saved.getTimeSpent(), saved.getTimeRemainingEstimate());
        return saved;
    }
}
