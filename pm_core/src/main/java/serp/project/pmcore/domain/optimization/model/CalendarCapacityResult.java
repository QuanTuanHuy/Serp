/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;

import java.util.List;

public record CalendarCapacityResult(
        List<ResourceCapacitySlot> slots,
        CapacityCoverageStatus coverageStatus,
        List<Long> fallbackUserIds,
        Long fetchedAt,
        List<OptimizationConstraintViolation> warnings
) {
}
