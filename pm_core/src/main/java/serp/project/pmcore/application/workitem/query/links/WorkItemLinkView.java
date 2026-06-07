/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.links;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.dto.WorkItemLinkProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemLinkView(
        Long id,
        String direction,
        LinkTypeSummary linkType,
        RelatedWorkItemSummary workItem
) {
    public static WorkItemLinkView from(Long currentWorkItemId, WorkItemLinkProjection link) {
        boolean outward = currentWorkItemId != null && currentWorkItemId.equals(link.sourceId());
        return new WorkItemLinkView(
                link.id(),
                outward ? "OUTWARD" : "INWARD",
                new LinkTypeSummary(
                        link.linkTypeId(),
                        link.linkTypeName(),
                        outward ? link.outwardDesc() : link.inwardDesc()
                ),
                new RelatedWorkItemSummary(
                        link.relatedWorkItemId(),
                        link.relatedProjectId(),
                        link.relatedWorkItemKey(),
                        link.relatedWorkItemSummary(),
                        link.relatedStatusId() != null ? new StatusSummary(
                                link.relatedStatusId(),
                                link.relatedStatusName(),
                                link.relatedStatusKey()
                        ) : null,
                        link.relatedPriorityId() != null ? new PrioritySummary(
                                link.relatedPriorityId(),
                                link.relatedPriorityName(),
                                link.relatedPriorityColor()
                        ) : null
                )
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LinkTypeSummary(Long id, String name, String description) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RelatedWorkItemSummary(
            Long id,
            Long projectId,
            String key,
            String summary,
            StatusSummary status,
            PrioritySummary priority
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatusSummary(Long id, String name, String key) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PrioritySummary(Long id, String name, String color) {
    }
}
