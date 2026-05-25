/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationPriorityScore(
        Long workItemId,
        double score,
        boolean neutralPriorityUsed
) {
}
