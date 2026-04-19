/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;

@Service
@RequiredArgsConstructor
public class CreateIssueTypeSchemeCommandHandler implements ICommandHandler<CreateIssueTypeSchemeCommand, IssueTypeSchemeView> {

    private final IIssueTypeSchemeService issueTypeSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueTypeSchemeView handle(CreateIssueTypeSchemeCommand command) {
        IssueTypeSchemeEntity created = issueTypeSchemeService.createIssueTypeScheme(
                IssueTypeSchemeEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .defaultIssueTypeId(command.defaultIssueTypeId())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return IssueTypeSchemeView.from(created, false);
    }
}
