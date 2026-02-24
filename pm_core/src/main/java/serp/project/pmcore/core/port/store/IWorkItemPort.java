/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkItemEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkItemPort {
    WorkItemEntity saveWorkItem(WorkItemEntity workItem);
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);
    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);
    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);
    void deleteWorkItemById(Long id, Long tenantId);
}
