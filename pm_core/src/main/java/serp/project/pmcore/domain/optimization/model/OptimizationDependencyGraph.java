/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record OptimizationDependencyGraph(
        List<OptimizationDependencyEdge> internalEdges,
        List<OptimizationDependencyEdge> externalEdges,
        List<List<Long>> cyclePaths,
        Map<Long, Set<Long>> predecessorsByWorkItemId,
        Map<Long, Set<Long>> successorsByWorkItemId,
        List<Long> topologicalOrder
) {
    public boolean hasCycles() {
        return cyclePaths != null && !cyclePaths.isEmpty();
    }
}
