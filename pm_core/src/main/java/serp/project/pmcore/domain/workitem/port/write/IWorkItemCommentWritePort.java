/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.write;

import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;

public interface IWorkItemCommentWritePort {
    WorkItemCommentEntity save(WorkItemCommentEntity comment);
}
