/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;

public record OptimizationConstraintViolation(
        OptimizationWarningCode code,
        Long workItemId,
        String message,
        String details
) {
}
