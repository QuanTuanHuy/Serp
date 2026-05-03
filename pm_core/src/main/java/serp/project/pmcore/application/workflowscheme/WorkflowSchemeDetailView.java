/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;

import java.util.List;
import java.util.Map;

public record WorkflowSchemeDetailView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultWorkflowId,
        boolean isSystem,
        boolean readOnly,
        List<WorkflowSchemeItemView> items,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkflowSchemeDetailView from(WorkflowSchemeEntity entity,
                                                Map<Long, WorkflowSchemeIssueTypeView> issueTypesById,
                                                Map<Long, WorkflowSchemeWorkflowView> workflowsById) {
        List<WorkflowSchemeItemView> itemViews = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                .map(item -> WorkflowSchemeItemView.from(
                        item,
                        issueTypesById.get(item.getIssueTypeId()),
                        workflowsById.get(item.getWorkflowId())
                ))
                .toList();

        return new WorkflowSchemeDetailView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultWorkflowId(),
                entity.isSystem(),
                entity.isSystem(),
                itemViews,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
