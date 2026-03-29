/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import java.util.List;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

public interface IWorkItemCustomFieldValuePort {
    List<WorkItemCustomFieldValueEntity> saveAll(List<WorkItemCustomFieldValueEntity> values);
}
