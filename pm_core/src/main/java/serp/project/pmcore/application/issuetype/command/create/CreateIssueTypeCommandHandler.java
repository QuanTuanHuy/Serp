/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.create;

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
public class CreateIssueTypeCommandHandler implements ICommandHandler<CreateIssueTypeCommand, IssueTypeView> {

    private final IIssueTypeService issueTypeService;
    private final IssueTypeOutboxPublisher issueTypeOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueTypeView handle(CreateIssueTypeCommand command) {
        IssueTypeEntity created = issueTypeService.createIssueType(
                IssueTypeEntity.builder()
                        .typeKey(command.typeKey())
                        .name(command.name())
                        .description(command.description())
                        .iconUrl(command.iconUrl())
                        .hierarchyLevel(command.hierarchyLevel())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        issueTypeOutboxPublisher.publishIssueTypeCreated(
                command.tenantId(),
                IssueTypeEventPayload.from(created, command.userId())
        );
        return IssueTypeView.from(created, false);
    }
}
