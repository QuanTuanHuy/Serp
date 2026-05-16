/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationDependencyEdge(
        Long predecessorId,
        Long successorId,
        Long issueLinkId,
        Long linkTypeId,
        boolean external
) {
}
