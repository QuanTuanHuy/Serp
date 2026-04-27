/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;

@Service
@RequiredArgsConstructor
public class DeleteIssueTypeSchemeCommandHandler implements ICommandHandler<DeleteIssueTypeSchemeCommand, DeleteIssueTypeSchemeResult> {

    private final IIssueTypeSchemeService issueTypeSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteIssueTypeSchemeResult handle(DeleteIssueTypeSchemeCommand command) {
        IssueTypeSchemeEntity deleted = issueTypeSchemeService.deleteIssueTypeScheme(
                command.schemeId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteIssueTypeSchemeResult.from(deleted);
    }
}
