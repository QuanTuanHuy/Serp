/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemCommentReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemCommentWritePort;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeleteWorkItemCommentCommandHandler implements ICommandHandler<DeleteWorkItemCommentCommand, Void> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IWorkItemCommentReadPort commentReadPort;
    private final IWorkItemCommentWritePort commentWritePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void handle(DeleteWorkItemCommentCommand command) {
        accessHelper.requireEditableWorkItem(command.projectId(), command.workItemId(), command.tenantId(), command.userId(), command.groupKeys());
        WorkItemCommentEntity comment = commentReadPort.findById(command.commentId(), command.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.workItemComment(command.commentId()));
        ensureSameWorkItem(comment, command.workItemId());
        ensureAuthor(comment, command.userId());
        Long now = Instant.now().toEpochMilli();
        comment.setDeletedAt(now);
        comment.setUpdatedAt(now);
        comment.setUpdatedBy(command.userId());
        commentWritePort.save(comment);
        return null;
    }

    private void ensureSameWorkItem(WorkItemCommentEntity comment, Long workItemId) {
        if (!Objects.equals(comment.getWorkItemId(), workItemId)) {
            throw ResourceNotFoundException.workItemComment(comment.getId());
        }
    }

    private void ensureAuthor(WorkItemCommentEntity comment, Long userId) {
        if (!Objects.equals(comment.getAuthorId(), userId)) {
            throw new AccessDeniedException(DomainErrorCode.WORK_ITEM_COMMENT_NOT_OWNER);
        }
    }
}
