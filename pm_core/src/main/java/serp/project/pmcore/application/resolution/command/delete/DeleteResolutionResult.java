/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.delete;

import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;

public record DeleteResolutionResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteResolutionResult from(ResolutionEntity entity) {
        return new DeleteResolutionResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
