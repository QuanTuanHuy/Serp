/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteIssueTypeSchemeCommand(
        Long schemeId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteIssueTypeSchemeResult> {
}
