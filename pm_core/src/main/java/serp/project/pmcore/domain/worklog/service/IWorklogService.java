/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;

public interface IWorklogService {
    WorklogEntity createWorklog(WorklogEntity worklog, Long tenantId, Long userId);

    WorklogEntity updateWorklog(WorklogEntity worklog, Long userId);

    WorklogEntity softDeleteWorklog(WorklogEntity worklog, Long userId, Long deletedAt);

    WorklogEntity getWorklogById(Long worklogId, Long tenantId);

    PageResult<WorklogEntity> listWorklogs(Long tenantId, WorklogListCriteria criteria);

    WorkItemEntity refreshWorkItemTimeTracking(WorkItemEntity workItem, Long userId);
}
