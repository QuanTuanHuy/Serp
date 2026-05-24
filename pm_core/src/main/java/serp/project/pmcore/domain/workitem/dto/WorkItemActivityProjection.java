/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record WorkItemActivityProjection(
        String id,
        String type,
        Long actorId,
        String body,
        String fieldKey,
        String fieldName,
        String fromValue,
        String toValue,
        Long createdAt
) {
}
