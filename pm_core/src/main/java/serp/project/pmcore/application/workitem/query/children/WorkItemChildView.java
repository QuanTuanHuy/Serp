/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.children;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.domain.workitem.dto.WorkItemChildProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemChildView(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        IssueTypeSummary issueType,
        StatusSummary status,
        PrioritySummary priority,
        UserSummary assignee
) {
    public static WorkItemChildView from(WorkItemChildProjection child, UserSummary assignee) {
        return new WorkItemChildView(
                child.id(),
                child.projectId(),
                child.parentId(),
                child.key(),
                child.summary(),
                child.issueTypeId() != null ? new IssueTypeSummary(
                        child.issueTypeId(),
                        child.issueTypeName(),
                        child.issueTypeIconUrl(),
                        child.issueTypeHierarchyLevel()
                ) : null,
                child.statusId() != null ? new StatusSummary(
                        child.statusId(),
                        child.statusName(),
                        child.statusKey()
                ) : null,
                child.priorityId() != null ? new PrioritySummary(
                        child.priorityId(),
                        child.priorityName(),
                        child.priorityColor()
                ) : null,
                assignee
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record IssueTypeSummary(Long id, String name, String iconUrl, Integer hierarchyLevel) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatusSummary(Long id, String name, String key) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PrioritySummary(Long id, String name, String color) {
    }
}

