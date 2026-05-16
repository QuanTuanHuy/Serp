/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;

public record OptimizationDuration(
        Long workItemId,
        long durationMillis,
        OptimizationConfidence confidence,
        String source
) {
}
