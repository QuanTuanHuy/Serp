/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.calendar;

public final class WorkItemCalendarSummaryViews {

    private WorkItemCalendarSummaryViews() {
    }

    public record UserSummaryView(
            Long id,
            String name,
            String avatarUrl
    ) {
    }

    public record IssueTypeSummaryView(
            Long id,
            String name,
            String iconUrl,
            Integer hierarchyLevel
    ) {
    }

    public record StatusSummaryView(
            Long id,
            String name
    ) {
    }

    public record PrioritySummaryView(
            Long id,
            String name,
            String color
    ) {
    }
}
