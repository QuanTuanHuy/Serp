/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import java.util.List;

public record ProjectSummaryStatusOverviewView(
        long total,
        List<ProjectSummaryBreakdownItemView> items
) {
    public static ProjectSummaryStatusOverviewView from(List<ProjectSummaryBreakdownItemView> items) {
        long total = items.stream()
                .mapToLong(ProjectSummaryBreakdownItemView::count)
                .sum();
        return new ProjectSummaryStatusOverviewView(total, items);
    }
}
