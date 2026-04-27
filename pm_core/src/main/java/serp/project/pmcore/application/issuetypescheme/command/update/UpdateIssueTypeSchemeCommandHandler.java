/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;

@Service
@RequiredArgsConstructor
public class UpdateIssueTypeSchemeCommandHandler implements ICommandHandler<UpdateIssueTypeSchemeCommand, IssueTypeSchemeView> {

    private final IIssueTypeSchemeService issueTypeSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueTypeSchemeView handle(UpdateIssueTypeSchemeCommand command) {
        IssueTypeSchemeEntity updated = issueTypeSchemeService.updateIssueTypeScheme(
                command.schemeId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return IssueTypeSchemeView.from(updated, false);
    }
}
