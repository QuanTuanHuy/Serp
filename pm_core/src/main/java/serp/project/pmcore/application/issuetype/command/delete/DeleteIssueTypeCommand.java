/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteIssueTypeCommand(
        Long issueTypeId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteIssueTypeResult> {
}
