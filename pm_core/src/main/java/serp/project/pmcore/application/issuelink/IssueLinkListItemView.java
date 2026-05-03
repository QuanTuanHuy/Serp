/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;

public record IssueLinkListItemView(
        Long linkId,
        String direction,
        Long linkTypeId,
        String linkTypeName,
        String description,
        RelatedWorkItemSummaryView relatedWorkItem,
        Long createdAt,
        Long createdBy
) {
    public static IssueLinkListItemView from(IssueLinkDetailEntity entity, Long ownerWorkItemId) {
        boolean outward = ownerWorkItemId != null && ownerWorkItemId.equals(entity.getSourceId());
        return new IssueLinkListItemView(
                entity.getLinkId(),
                outward ? "OUTWARD" : "INWARD",
                entity.getLinkTypeId(),
                entity.getLinkTypeName(),
                outward ? entity.getOutwardDescription() : entity.getInwardDescription(),
                new RelatedWorkItemSummaryView(
                        entity.getRelatedWorkItemId(),
                        entity.getRelatedProjectId(),
                        entity.getRelatedWorkItemKey(),
                        entity.getRelatedWorkItemSummary(),
                        entity.getRelatedIssueTypeId(),
                        entity.getRelatedIssueTypeName(),
                        entity.getRelatedStatusId(),
                        entity.getRelatedStatusName()
                ),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    public record RelatedWorkItemSummaryView(
            Long id,
            Long projectId,
            String key,
            String summary,
            Long issueTypeId,
            String issueTypeName,
            Long statusId,
            String statusName
    ) {
    }
}
