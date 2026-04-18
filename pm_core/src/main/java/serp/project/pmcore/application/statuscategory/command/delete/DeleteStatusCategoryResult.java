/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.delete;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;

public record DeleteStatusCategoryResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteStatusCategoryResult from(StatusCategoryEntity entity) {
        return new DeleteStatusCategoryResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
