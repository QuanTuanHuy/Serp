/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.internal;

public record TransitionSubjectContext(
        Long projectId,
        Long workflowSchemeId,
        Long workItemId,
        Long issueTypeId,
        Long workflowStepId,
        Long statusId
) {
}
