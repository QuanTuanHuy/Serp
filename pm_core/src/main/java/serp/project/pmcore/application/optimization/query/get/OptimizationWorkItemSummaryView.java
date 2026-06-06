/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

public record OptimizationWorkItemSummaryView(
        Long id,
        String key,
        String summary,
        String issueTypeName,
        String statusName,
        String priorityName
) {
}
