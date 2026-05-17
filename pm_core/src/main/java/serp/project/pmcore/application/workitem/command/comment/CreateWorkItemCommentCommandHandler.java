/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.query.comment.WorkItemCommentView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemCommentWritePort;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateWorkItemCommentCommandHandler implements ICommandHandler<CreateWorkItemCommentCommand, WorkItemCommentView> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IWorkItemCommentWritePort commentWritePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkItemCommentView handle(CreateWorkItemCommentCommand command) {
        accessHelper.requireEditableWorkItem(command.projectId(), command.workItemId(), command.tenantId(), command.userId(), command.groupKeys());
        String body = normalizeBody(command.body());
        Long now = Instant.now().toEpochMilli();
        WorkItemCommentEntity saved = commentWritePort.save(WorkItemCommentEntity.builder()
                .tenantId(command.tenantId())
                .workItemId(command.workItemId())
                .authorId(command.userId())
                .body(body)
                .createdAt(now)
                .createdBy(command.userId())
                .updatedAt(now)
                .updatedBy(command.userId())
                .build());
        return WorkItemCommentView.from(saved, new WorkItemCommentView.UserSummaryView(command.userId(), null, null));
    }

    private String normalizeBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new DomainValidationException(DomainErrorCode.WORK_ITEM_COMMENT_EMPTY);
        }
        return body.trim();
    }
}
