/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor;

import lombok.Builder;

@Builder
public record RoleActorEventPayload(
        Long projectId,
        Long roleId,
        String subjectType,
        String subjectId,
        Long actorId,
        Long performedBy,
        Long occurredAt
) {
}
