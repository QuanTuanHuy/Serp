/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.delete;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;

public record DeletePriorityResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeletePriorityResult from(PriorityEntity entity) {
        return new DeletePriorityResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
