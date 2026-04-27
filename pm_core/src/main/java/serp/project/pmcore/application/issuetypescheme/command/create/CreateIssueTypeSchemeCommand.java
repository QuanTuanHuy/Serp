/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.create;

import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateIssueTypeSchemeCommand(
        String name,
        String description,
        Long defaultIssueTypeId,
        Long tenantId,
        Long userId
) implements ICommand<IssueTypeSchemeView> {
}
