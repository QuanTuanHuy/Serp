/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;

import java.util.List;

public interface IWorkItemCustomFieldValuePort {
    List<WorkItemCustomFieldValueEntity> saveAll(List<WorkItemCustomFieldValueEntity> values);
}
