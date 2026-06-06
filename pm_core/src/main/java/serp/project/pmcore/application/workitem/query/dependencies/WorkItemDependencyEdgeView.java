/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

public record WorkItemDependencyEdgeView(
        Long linkId,
        Long sourceId,
        Long targetId,
        Long predecessorId,
        Long successorId,
        Long linkTypeId,
        String linkTypeName,
        String dependencyBehavior,
        boolean outsideFilter,
        boolean externalProject,
        boolean relatedLink,
        boolean cycle
) {
}
