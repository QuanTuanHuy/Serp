/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import java.util.Collection;
import java.util.List;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

public interface IWorkItemCustomFieldValuePort {
    List<WorkItemCustomFieldValueEntity> getActiveValuesByWorkItemId(Long workItemId, Long tenantId);

    List<WorkItemCustomFieldValueEntity> saveAll(List<WorkItemCustomFieldValueEntity> values);

    void softDeleteByWorkItemIdAndCustomFieldIds(Long workItemId,
                                                 Collection<Long> customFieldIds,
                                                 Long updatedBy,
                                                 Long deletedAt);
}
