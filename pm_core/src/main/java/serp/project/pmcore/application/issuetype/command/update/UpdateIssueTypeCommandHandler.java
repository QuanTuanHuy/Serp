/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.issuetype.command.IssueTypeEventPayload;
import serp.project.pmcore.application.issuetype.command.IssueTypeOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;

@Service
@RequiredArgsConstructor
public class UpdateIssueTypeCommandHandler implements ICommandHandler<UpdateIssueTypeCommand, IssueTypeView> {

    private final IIssueTypeService issueTypeService;
    private final IssueTypeOutboxPublisher issueTypeOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueTypeView handle(UpdateIssueTypeCommand command) {
        IssueTypeEntity updated = issueTypeService.updateIssueType(
                command.issueTypeId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        issueTypeOutboxPublisher.publishIssueTypeUpdated(
                command.tenantId(),
                IssueTypeEventPayload.from(updated, command.userId())
        );
        return IssueTypeView.from(updated, false);
    }
}
