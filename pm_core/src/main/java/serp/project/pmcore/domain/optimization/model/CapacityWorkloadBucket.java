/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record CapacityWorkloadBucket(
        Long assigneeId,
        Long timeBucketStart,
        Long timeBucketEnd,
        Long sameProjectOutsideScopeReservedMillis,
        Long crossProjectReservedMillis,
        Long totalReservedMillis
) {
}
