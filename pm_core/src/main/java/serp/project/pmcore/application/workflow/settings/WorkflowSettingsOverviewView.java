/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.settings;

import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;

import java.util.List;

public record WorkflowSettingsOverviewView(
        List<WorkflowView> workflows,
        List<WorkflowSchemeView> workflowSchemes
) {
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
            List<SchemeRefView> relatedSchemes,
            List<ProjectRefView> spaces,
            Long createdAt,
            Long createdBy,
            Long updatedAt,
            Long updatedBy
    ) {
    }

    public record WorkflowSchemeView(
            Long id,
            Long tenantId,
            String name,
            String description,
            Long defaultWorkflowId,
            boolean isSystem,
            boolean readOnly,
            List<WorkflowSchemeItemView> items,
            List<ProjectRefView> spaces,
            Long createdAt,
            Long createdBy,
            Long updatedAt,
            Long updatedBy
    ) {
    }

    public record SchemeRefView(
            Long id,
            String name,
            boolean isSystem
    ) {
    }

    public record WorkflowSchemeItemView(
            Long id,
            Long issueTypeId,
            Long workflowId,
            WorkTypeOptionView workType,
            WorkflowOptionView workflow
    ) {
    }

    public record WorkTypeOptionView(
            Long id,
            String typeKey,
            String name,
            Integer hierarchyLevel,
            boolean isSystem,
            boolean readOnly
    ) {
    }

    public record WorkflowOptionView(
            Long id,
            String workflowKey,
            String name,
            String description,
            Long currentPublishedVersionId,
            Long draftVersionId,
            WorkflowLifecycleState lifecycleState,
            boolean isSystem,
            boolean readOnly
    ) {
    }

    public record ProjectRefView(
            Long id,
            String key,
            String name
    ) {
    }
}
