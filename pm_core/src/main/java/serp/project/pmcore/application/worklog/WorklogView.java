/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog;

import serp.project.pmcore.domain.worklog.entity.WorklogEntity;

public record WorklogView(
        Long id,
        Long workItemId,
        Long authorId,
        String comment,
        Long startDate,
        Long timeSpent,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorklogView from(WorklogEntity worklog) {
        return new WorklogView(
                worklog.getId(),
                worklog.getWorkItemId(),
                worklog.getAuthorId(),
                worklog.getComment(),
                worklog.getStartDate(),
                worklog.getTimeSpent(),
                worklog.getCreatedAt(),
                worklog.getCreatedBy(),
                worklog.getUpdatedAt(),
                worklog.getUpdatedBy()
        );
    }
}
