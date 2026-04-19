/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.manageitems;

import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeDetailView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.List;

public record ManageIssueTypeSchemeItemsCommand(
        Long schemeId,
        List<Long> issueTypeIds,
        Long tenantId,
        Long userId
) implements ICommand<IssueTypeSchemeDetailView> {
}
