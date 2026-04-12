/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;

public record PriorityEventPayload(
        Long priorityId,
        String priorityKey,
        String name,
        Integer sequence,
        String color,
        boolean isSystem,
        Long performedBy,
        Long deletedAt
) {
    public static PriorityEventPayload from(PriorityEntity entity, Long performedBy) {
        return new PriorityEventPayload(
                entity.getId(),
                entity.getPriorityKey(),
                entity.getName(),
                entity.getSequence(),
                entity.getColor(),
                entity.isSystem(),
                performedBy,
                entity.getDeletedAt()
        );
    }
}
