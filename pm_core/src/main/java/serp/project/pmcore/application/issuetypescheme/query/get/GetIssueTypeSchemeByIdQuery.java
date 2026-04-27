/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.query.get;

import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeDetailView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetIssueTypeSchemeByIdQuery(
        Long schemeId,
        Long tenantId
) implements IQuery<IssueTypeSchemeDetailView> {
}
