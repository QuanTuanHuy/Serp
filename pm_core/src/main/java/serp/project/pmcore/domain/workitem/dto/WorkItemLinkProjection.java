/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record WorkItemLinkProjection(
        Long id,
        Long sourceId,
        Long targetId,
        Long linkTypeId,
        String linkTypeName,
        String outwardDesc,
        String inwardDesc,
        Long relatedWorkItemId,
        Long relatedProjectId,
        String relatedWorkItemKey,
        String relatedWorkItemSummary,
        Long relatedStatusId,
        String relatedStatusKey,
        String relatedStatusName,
        Long relatedPriorityId,
        String relatedPriorityName,
        String relatedPriorityColor
) {
}
