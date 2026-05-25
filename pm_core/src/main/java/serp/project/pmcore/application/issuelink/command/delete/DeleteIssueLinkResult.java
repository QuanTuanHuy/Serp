/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.delete;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;

public record DeleteIssueLinkResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteIssueLinkResult from(IssueLinkEntity entity) {
        return new DeleteIssueLinkResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
