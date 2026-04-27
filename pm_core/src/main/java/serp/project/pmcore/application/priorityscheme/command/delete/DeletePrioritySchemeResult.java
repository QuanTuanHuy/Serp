/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.delete;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

public record DeletePrioritySchemeResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeletePrioritySchemeResult from(PrioritySchemeEntity entity) {
        return new DeletePrioritySchemeResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
