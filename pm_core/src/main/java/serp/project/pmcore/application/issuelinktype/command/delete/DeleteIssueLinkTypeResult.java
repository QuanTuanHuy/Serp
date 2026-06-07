/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.delete;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;

public record DeleteIssueLinkTypeResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteIssueLinkTypeResult from(IssueLinkTypeEntity entity) {
        return new DeleteIssueLinkTypeResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
