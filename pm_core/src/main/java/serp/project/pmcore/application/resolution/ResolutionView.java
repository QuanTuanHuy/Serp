/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution;

import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;

public record ResolutionView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Integer sequence,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ResolutionView from(ResolutionEntity entity) {
        return from(entity, Boolean.TRUE.equals(entity.getIsSystem()));
    }

    public static ResolutionView from(ResolutionEntity entity, boolean readOnly) {
        return new ResolutionView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSequence(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
