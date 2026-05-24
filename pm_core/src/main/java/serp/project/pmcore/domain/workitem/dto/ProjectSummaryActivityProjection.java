/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record ProjectSummaryActivityProjection(
        String id,
        String type,
        Long actorId,
        Long workItemId,
        String workItemKey,
        String workItemSummary,
        Long statusId,
        String statusKey,
        String statusName,
        String body,
        String fieldKey,
        String fieldName,
        String fromValue,
        String toValue,
        Long createdAt
) {
}
