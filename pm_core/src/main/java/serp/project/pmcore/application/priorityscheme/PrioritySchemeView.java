/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

public record PrioritySchemeView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultPriorityId,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static PrioritySchemeView from(PrioritySchemeEntity entity) {
        return from(entity, entity.isSystem());
    }

    public static PrioritySchemeView from(PrioritySchemeEntity entity, boolean readOnly) {
        return new PrioritySchemeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultPriorityId(),
                entity.isSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
