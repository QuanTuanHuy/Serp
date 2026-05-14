/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemTimelineDependencyProjection(
        Long linkId,
        Long sourceId,
        Long targetId,
        Long linkTypeId,
        String linkTypeName,
        String description
) {
}
