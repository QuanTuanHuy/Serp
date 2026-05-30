/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.settings;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetIssueTypeSettingsOverviewQuery(
        Long tenantId
) implements IQuery<IssueTypeSettingsOverviewView> {
}
