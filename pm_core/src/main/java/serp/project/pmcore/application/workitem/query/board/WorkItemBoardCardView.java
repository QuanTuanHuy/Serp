/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemBoardCardView(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        String description,
        Long assigneeId,
        Long reporterId,
        Long startDate,
        Long dueDate,
        String rank,
        IssueTypeSummaryView issueType,
        PrioritySummaryView priority
) {

    public static WorkItemBoardCardView from(WorkItemBoardItemProjection projection) {
        return new WorkItemBoardCardView(
                projection.id(),
                projection.projectId(),
                projection.parentId(),
                projection.key(),
                projection.summary(),
                projection.description(),
                projection.assigneeId(),
                projection.reporterId(),
                projection.startDate(),
                projection.dueDate(),
                projection.rank(),
                new IssueTypeSummaryView(
                        projection.issueTypeId(),
                        projection.issueTypeName(),
                        projection.issueTypeIconUrl(),
                        projection.issueTypeHierarchyLevel()
                ),
                new PrioritySummaryView(
                        projection.priorityId(),
                        projection.priorityName(),
                        projection.priorityIconUrl(),
                        projection.priorityColor()
                )
        );
    }

    public record IssueTypeSummaryView(
            Long id,
            String name,
            String iconUrl,
            Integer hierarchyLevel
    ) {
    }

    public record PrioritySummaryView(
            Long id,
            String name,
            String iconUrl,
            String color
    ) {
    }
}
