/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;

public record PriorityView(
        Long id,
        Long tenantId,
        String priorityKey,
        String name,
        String description,
        String iconUrl,
        String color,
        Integer sequence,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static PriorityView from(PriorityEntity entity) {
        return from(entity, entity.isSystem());
    }

    public static PriorityView from(PriorityEntity entity, boolean readOnly) {
        return new PriorityView(
                entity.getId(),
                entity.getTenantId(),
                entity.getPriorityKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getIconUrl(),
                entity.getColor(),
                entity.getSequence(),
                entity.isSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
