/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;

public record WorklogEventPayload(
        Long worklogId,
        Long workItemId,
        Long projectId,
        Long authorId,
        Long timeSpent,
        Long startDate,
        String comment,
        Long performedBy,
        Long occurredAt,
        Long deletedAt
) {
    public static WorklogEventPayload from(WorklogEntity worklog,
                                           WorkItemEntity workItem,
                                           Long performedBy,
                                           Long occurredAt,
                                           Long deletedAt) {
        return new WorklogEventPayload(
                worklog.getId(),
                worklog.getWorkItemId(),
                workItem.getProjectId(),
                worklog.getAuthorId(),
                worklog.getTimeSpent(),
                worklog.getStartDate(),
                worklog.getComment(),
                performedBy,
                occurredAt,
                deletedAt
        );
    }
}
