/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.assign;

import lombok.Builder;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

@Builder
public record AssignWorkItemResult(
        Long id,
        Long projectId,
        String key,
        Long assigneeId,
        Long updatedAt,
        Long updatedBy
) {
    public static AssignWorkItemResult from(WorkItemEntity workItem) {
        return new AssignWorkItemResult(
                workItem.getId(),
                workItem.getProjectId(),
                workItem.getKey(),
                workItem.getAssigneeId(),
                workItem.getUpdatedAt(),
                workItem.getUpdatedBy()
        );
    }
}
