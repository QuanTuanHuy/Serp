/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.delete;

import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public record DeleteStatusResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteStatusResult from(StatusEntity entity) {
        return new DeleteStatusResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
