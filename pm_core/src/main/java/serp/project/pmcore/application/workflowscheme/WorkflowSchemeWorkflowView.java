/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;

public record WorkflowSchemeWorkflowView(
        Long id,
        Long tenantId,
        String workflowKey,
        String name,
        String description,
        Long currentPublishedVersionId,
        WorkflowLifecycleState lifecycleState,
        boolean isSystem,
        boolean readOnly
) {
    public static WorkflowSchemeWorkflowView from(WorkflowEntity entity) {
        return new WorkflowSchemeWorkflowView(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkflowKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getCurrentPublishedVersionId(),
                entity.getLifecycleState(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                Boolean.TRUE.equals(entity.getIsSystem())
        );
    }
}
