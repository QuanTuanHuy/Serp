/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;

public record WorkItemTimelineDependencyView(
        Long linkId,
        Long sourceId,
        Long targetId,
        Long linkTypeId,
        String linkTypeName,
        String description
) {

    public static WorkItemTimelineDependencyView from(WorkItemTimelineDependencyProjection projection) {
        return new WorkItemTimelineDependencyView(
                projection.linkId(),
                projection.sourceId(),
                projection.targetId(),
                projection.linkTypeId(),
                projection.linkTypeName(),
                projection.description()
        );
    }
}
