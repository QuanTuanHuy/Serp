/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.dto;

public record WorkItemStatusChangeNotificationContext(
        Long transitionId,
        String transitionName,
        Long currentStepId,
        Long targetStepId,
        Long targetStatusId,
        String targetStatusKey,
        String targetStatusName,
        String targetStatusCategoryKey,
        String targetStatusCategoryName,
        Long resolutionId
) {
}
