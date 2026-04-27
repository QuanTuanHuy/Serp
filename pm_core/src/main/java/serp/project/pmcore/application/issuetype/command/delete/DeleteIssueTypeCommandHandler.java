/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetype.command.IssueTypeEventPayload;
import serp.project.pmcore.application.issuetype.command.IssueTypeOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;

@Service
@RequiredArgsConstructor
public class DeleteIssueTypeCommandHandler implements ICommandHandler<DeleteIssueTypeCommand, DeleteIssueTypeResult> {

    private final IIssueTypeService issueTypeService;
    private final IssueTypeOutboxPublisher issueTypeOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteIssueTypeResult handle(DeleteIssueTypeCommand command) {
        IssueTypeEntity deleted = issueTypeService.deleteIssueType(
                command.issueTypeId(),
                command.tenantId(),
                command.userId()
        );
        issueTypeOutboxPublisher.publishIssueTypeDeleted(
                command.tenantId(),
                IssueTypeEventPayload.from(deleted, command.userId())
        );
        return DeleteIssueTypeResult.from(deleted);
    }
}
