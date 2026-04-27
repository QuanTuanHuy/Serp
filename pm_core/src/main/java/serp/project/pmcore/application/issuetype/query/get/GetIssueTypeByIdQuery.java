/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.query.get;

import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetIssueTypeByIdQuery(
        Long issueTypeId,
        Long tenantId
) implements IQuery<IssueTypeView> {
}
