/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record ResourceCapacitySlot(
        Long assigneeId,
        Long slotStart,
        Long slotEnd,
        long capacityMillis
) {
}
