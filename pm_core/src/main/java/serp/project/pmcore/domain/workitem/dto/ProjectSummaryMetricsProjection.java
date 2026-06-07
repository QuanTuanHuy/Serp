/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record ProjectSummaryMetricsProjection(
        long completedLast7Days,
        long updatedLast7Days,
        long createdLast7Days,
        long dueSoonNext7Days
) {
}
