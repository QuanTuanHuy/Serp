/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.query.WorkItemSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface IWorkItemReadPort {
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);

    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);

    Optional<String> getLastRankByProjectId(Long projectId, Long tenantId);

    PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria);
}
