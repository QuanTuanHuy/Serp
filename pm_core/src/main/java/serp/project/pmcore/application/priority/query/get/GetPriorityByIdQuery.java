/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.query.get;

import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetPriorityByIdQuery(
        Long priorityId,
        Long tenantId
) implements IQuery<PriorityView> {
}
