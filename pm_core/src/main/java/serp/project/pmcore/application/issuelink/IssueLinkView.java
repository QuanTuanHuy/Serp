/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;

public record IssueLinkView(
        Long id,
        Long sourceId,
        Long targetId,
        Long linkTypeId,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static IssueLinkView from(IssueLinkEntity entity) {
        return new IssueLinkView(
                entity.getId(),
                entity.getSourceId(),
                entity.getTargetId(),
                entity.getLinkTypeId(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
