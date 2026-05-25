/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.domain.workitem.dto.ProjectSummaryParentOptionProjection;

public record ProjectSummaryParentOptionView(
        Long id,
        String key,
        String summary
) {
    public static ProjectSummaryParentOptionView from(ProjectSummaryParentOptionProjection projection) {
        return new ProjectSummaryParentOptionView(
                projection.id(),
                projection.key(),
                projection.summary()
        );
    }
}
