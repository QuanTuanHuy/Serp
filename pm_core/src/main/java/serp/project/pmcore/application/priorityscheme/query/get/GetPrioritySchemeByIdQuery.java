/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.query.get;

import serp.project.pmcore.application.priorityscheme.PrioritySchemeDetailView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetPrioritySchemeByIdQuery(
        Long schemeId,
        Long tenantId
) implements IQuery<PrioritySchemeDetailView> {
}
