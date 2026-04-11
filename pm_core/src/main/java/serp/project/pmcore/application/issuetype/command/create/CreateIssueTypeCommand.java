/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.create;

import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateIssueTypeCommand(
        String typeKey,
        String name,
        String description,
        String iconUrl,
        Integer hierarchyLevel,
        Long tenantId,
        Long userId
) implements ICommand<IssueTypeView> {
}
