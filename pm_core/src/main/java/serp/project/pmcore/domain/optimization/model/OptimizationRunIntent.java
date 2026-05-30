/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

public record OptimizationRunIntent(
        String algorithmKey,
        OptimizationObjective objective,
        OptimizationChangeScope changeScope
) {
}
