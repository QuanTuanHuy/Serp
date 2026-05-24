/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.comment;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record DeleteWorkItemCommentCommand(
        Long projectId,
        Long workItemId,
        Long commentId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<Void> {
}
