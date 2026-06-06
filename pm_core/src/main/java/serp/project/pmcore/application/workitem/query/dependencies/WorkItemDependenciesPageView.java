/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

import java.util.List;

public record WorkItemDependenciesPageView(
        Long projectId,
        List<WorkItemDependencyNodeView> nodes,
        List<WorkItemDependencyEdgeView> edges,
        WorkItemDependencySummaryView summary,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize,
        int depth,
        boolean includeOutside,
        boolean includeRelatedLinks
) {
}
