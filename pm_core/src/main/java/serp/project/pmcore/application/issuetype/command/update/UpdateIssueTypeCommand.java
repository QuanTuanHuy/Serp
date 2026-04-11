/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.update;

import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;

public record UpdateIssueTypeCommand(
        Long issueTypeId,
        IssueTypeUpdateData data,
        boolean typeKeyProvided,
        boolean tenantIdProvided,
        boolean isSystemProvided,
        Long tenantId,
        Long userId
) implements ICommand<IssueTypeView> {
}
