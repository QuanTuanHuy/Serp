/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetWorkItemByIdQuery(
    Long tenantId,
    Long userId,
    Long projectId,
    Long workItemId
) implements IQuery<WorkItemDetailView> {
}
