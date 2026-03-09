/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.pmcore.core.domain.dto.filter.WorkItemFilterRequest;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkItemPort {
    WorkItemEntity saveWorkItem(WorkItemEntity workItem);
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);
    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);
    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);
    void deleteWorkItemById(Long id, Long tenantId);
    Pair<List<WorkItemEntity>, Long> searchWorkItems(Long tenantId, WorkItemFilterRequest filter);
}
