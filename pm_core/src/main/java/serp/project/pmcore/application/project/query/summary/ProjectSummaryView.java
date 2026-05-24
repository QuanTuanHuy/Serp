/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.application.shared.pagination.PageView;

import java.util.List;

public record ProjectSummaryView(
        Long projectId,
        ProjectSummaryMetricsView metrics,
        ProjectSummaryStatusOverviewView statusOverview,
        List<ProjectSummaryBreakdownItemView> priorityBreakdown,
        List<ProjectSummaryBreakdownItemView> workTypeBreakdown,
        PageView<ProjectSummaryActivityView> recentActivity,
        ProjectSummaryFilterOptionsView filterOptions
) {
}
