/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.write;

import serp.project.pmcore.domain.workitem.entity.WorkItemHistoryEntity;

import java.util.List;

public interface IWorkItemHistoryWritePort {
    List<WorkItemHistoryEntity> saveAll(List<WorkItemHistoryEntity> histories);
}
