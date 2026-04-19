/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;

public record PrioritySchemePriorityView(
        Long id,
        Long tenantId,
        String priorityKey,
        String name,
        String color,
        Integer sequence,
        boolean isSystem,
        boolean readOnly
) {
    public static PrioritySchemePriorityView from(PriorityEntity entity) {
        return new PrioritySchemePriorityView(
                entity.getId(),
                entity.getTenantId(),
                entity.getPriorityKey(),
                entity.getName(),
                entity.getColor(),
                entity.getSequence(),
                entity.isSystem(),
                entity.isSystem()
        );
    }
}
