/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;

import java.util.List;

public record CapacityResolutionResult(
        List<ResourceCapacitySlot> slots,
        CapacitySourceMode sourceMode,
        CapacityCoverageStatus calendarCoverageStatus,
        CapacityCoverageStatus workloadCoverageStatus,
        List<Long> fallbackUserIds,
        Long calendarFetchedAt,
        Long workloadFetchedAt,
        Long deductedWorkloadMillis,
        Long sameProjectOutsideScopeDeductedMillis,
        Long crossProjectDeductedMillis,
        List<CapacityWorkloadBucket> workloadBuckets,
        List<OptimizationConstraintViolation> warnings
) {
}
