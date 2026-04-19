/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;

public record WorkflowView(
        Long id,
        Long tenantId,
        String workflowKey,
        String name,
        String description,
        Long currentPublishedVersionId,
        Long draftVersionId,
        WorkflowLifecycleState lifecycleState,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkflowView from(WorkflowEntity entity) {
        return from(entity, Boolean.TRUE.equals(entity.getIsSystem()));
    }

    public static WorkflowView from(WorkflowEntity entity, boolean readOnly) {
        return new WorkflowView(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkflowKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getCurrentPublishedVersionId(),
                entity.getDraftVersionId(),
                entity.getLifecycleState(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
