/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.update;

import serp.project.pmcore.application.worklog.WorklogDetailView;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;

public record UpdateWorklogResult(
        Long id,
        Long workItemId,
        Long authorId,
        String comment,
        Long startDate,
        Long timeSpent,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy,
        Long workItemTimeSpent,
        Long workItemTimeRemainingEstimate
) {
    public static UpdateWorklogResult from(WorklogEntity worklog, WorkItemEntity workItem) {
        WorklogDetailView view = WorklogDetailView.from(worklog, workItem);
        return new UpdateWorklogResult(
                view.id(),
                view.workItemId(),
                view.authorId(),
                view.comment(),
                view.startDate(),
                view.timeSpent(),
                view.createdAt(),
                view.createdBy(),
                view.updatedAt(),
                view.updatedBy(),
                view.workItemTimeSpent(),
                view.workItemTimeRemainingEstimate()
        );
    }
}
