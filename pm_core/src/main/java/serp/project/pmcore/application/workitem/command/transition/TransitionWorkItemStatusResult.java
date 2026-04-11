/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition;

import java.util.List;

public record TransitionWorkItemStatusResult(
    Long id,
    Long projectId,
    String key,
    String summary,
    String description,
    Long workflowStepId,
    Long statusId,
    Long assigneeId,
    Long resolutionId,
    Long securityLevelId,
    Long dueDate,
    Long timeOriginalEstimate,
    Long transitionId,
    String transitionName,
    Long fromStepId,
    Long toStepId,
    List<String> changedFields,
    Long transitionedAt,
    Long transitionedBy
) {
}
