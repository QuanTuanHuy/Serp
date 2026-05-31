/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationProblem(
        OptimizationProjectModel projectModel,
        OptimizationBuilderInput input
) {
}
