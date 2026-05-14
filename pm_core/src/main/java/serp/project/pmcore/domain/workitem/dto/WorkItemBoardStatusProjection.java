/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemBoardStatusProjection(
        Long statusId,
        String statusKey,
        String statusName,
        String statusDescription,
        String statusIconUrl,
        Long statusCategoryId,
        String statusCategoryKey,
        String statusCategoryName
) {
}
