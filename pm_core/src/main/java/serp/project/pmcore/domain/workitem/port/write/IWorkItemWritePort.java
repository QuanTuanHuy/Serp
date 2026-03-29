/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.write;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IWorkItemWritePort {
    WorkItemEntity saveWorkItem(WorkItemEntity workItem);

    void deleteWorkItemById(Long id, Long tenantId);
}
