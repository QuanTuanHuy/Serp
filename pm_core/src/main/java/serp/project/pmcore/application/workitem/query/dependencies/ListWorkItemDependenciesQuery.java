/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.workitem.dto.WorkItemDependencyCriteria;

import java.util.Set;

public record ListWorkItemDependenciesQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemDependencyCriteria criteria
) implements IQuery<WorkItemDependenciesPageView> {
}
