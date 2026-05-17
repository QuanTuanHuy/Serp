/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.children;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.List;
import java.util.Set;

public record ListWorkItemChildrenQuery(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<List<WorkItemChildView>> {
}
