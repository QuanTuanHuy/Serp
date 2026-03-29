/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import org.springframework.data.util.Pair;
import serp.project.pmcore.application.workitem.query.search.model.WorkItemFilterRequest;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkItemReadPort {
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);

    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);

    Optional<String> getLastRankByProjectId(Long projectId, Long tenantId);

    Pair<List<WorkItemEntity>, Long> searchWorkItems(Long tenantId, WorkItemFilterRequest filter);
}
