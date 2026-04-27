/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.update;

import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;

public record UpdateIssueTypeSchemeCommand(
        Long schemeId,
        IssueTypeSchemeUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<IssueTypeSchemeView> {
}
