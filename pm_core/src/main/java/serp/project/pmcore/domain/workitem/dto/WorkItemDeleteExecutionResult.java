package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemDeleteExecutionResult(
        Integer deletedWorkItemCount,
        Integer deletedRelationCount,
        Integer deletedLinkCount
) {
}
