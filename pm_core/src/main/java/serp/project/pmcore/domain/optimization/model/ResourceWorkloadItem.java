/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record ResourceWorkloadItem(
        Long id,
        Long projectId,
        Long assigneeId,
        Long timeOriginalEstimate,
        Long timeRemainingEstimate
) {
}
