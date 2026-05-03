/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;

public record WorkflowSchemeView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultWorkflowId,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkflowSchemeView from(WorkflowSchemeEntity entity) {
        return from(entity, entity.isSystem());
    }

    public static WorkflowSchemeView from(WorkflowSchemeEntity entity, boolean readOnly) {
        return new WorkflowSchemeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultWorkflowId(),
                entity.isSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
