/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;

public record IssueLinkEventPayload(
        Long issueLinkId,
        Long sourceId,
        Long targetId,
        Long linkTypeId,
        Long actorUserId,
        Long occurredAt,
        Long deletedAt
) {
    public static IssueLinkEventPayload from(IssueLinkEntity entity, Long actorUserId, Long deletedAt) {
        return new IssueLinkEventPayload(
                entity.getId(),
                entity.getSourceId(),
                entity.getTargetId(),
                entity.getLinkTypeId(),
                actorUserId,
                deletedAt == null ? entity.getUpdatedAt() : deletedAt,
                deletedAt
        );
    }
}
