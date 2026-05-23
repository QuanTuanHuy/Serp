/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.domain.workitem.dto.ProjectSummaryMetricsProjection;

public record ProjectSummaryMetricsView(
        long completedLast7Days,
        long updatedLast7Days,
        long createdLast7Days,
        long dueSoonNext7Days
) {
    public static ProjectSummaryMetricsView from(ProjectSummaryMetricsProjection projection) {
        if (projection == null) {
            return new ProjectSummaryMetricsView(0L, 0L, 0L, 0L);
        }
        return new ProjectSummaryMetricsView(
                projection.completedLast7Days(),
                projection.updatedLast7Days(),
                projection.createdLast7Days(),
                projection.dueSoonNext7Days()
        );
    }
}
