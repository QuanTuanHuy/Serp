/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

public record WorkItemDependencySummaryView(
        long nodeCount,
        long dependencyCount,
        long outsideDependencyCount,
        long blockerCount,
        long blockedItemCount,
        long relatedLinkCount,
        long cycleCount
) {
}
