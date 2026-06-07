/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.domain.workitem.dto.ProjectSummaryBreakdownProjection;

public record ProjectSummaryBreakdownItemView(
        Long id,
        String key,
        String name,
        String iconUrl,
        String color,
        Integer sequence,
        String categoryKey,
        String categoryName,
        long count
) {
    public static ProjectSummaryBreakdownItemView from(ProjectSummaryBreakdownProjection projection) {
        return new ProjectSummaryBreakdownItemView(
                projection.id(),
                projection.key(),
                projection.name(),
                projection.iconUrl(),
                projection.color(),
                projection.sequence(),
                projection.categoryKey(),
                projection.categoryName(),
                projection.count()
        );
    }
}
