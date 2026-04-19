/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.port;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;

import java.util.Optional;

public interface IWorklogPort {
    WorklogEntity saveWorklog(WorklogEntity worklog);

    Optional<WorklogEntity> getWorklogById(Long worklogId, Long tenantId);

    PageResult<WorklogEntity> listWorklogs(Long tenantId, WorklogListCriteria criteria);

    long sumActiveTimeSpentByWorkItemId(Long workItemId, Long tenantId);
}
