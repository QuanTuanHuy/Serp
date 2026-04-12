/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.delete;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;

public record DeleteWorklogResult(
        Long worklogId,
        Long deletedAt,
        Long deletedBy,
        Long workItemId,
        Long workItemTimeSpent,
        Long workItemTimeRemainingEstimate
) {
    public static DeleteWorklogResult from(WorklogEntity worklog,
                                           WorkItemEntity workItem,
                                           Long deletedBy,
                                           Long deletedAt) {
        return new DeleteWorklogResult(
                worklog.getId(),
                deletedAt,
                deletedBy,
                workItem.getId(),
                workItem.getTimeSpent(),
                workItem.getTimeRemainingEstimate()
        );
    }
}
