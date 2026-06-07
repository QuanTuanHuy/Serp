/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.comment;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workitem.query.comment.WorkItemCommentView;

import java.util.Set;

public record CreateWorkItemCommentCommand(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String body
) implements ICommand<WorkItemCommentView> {
}
