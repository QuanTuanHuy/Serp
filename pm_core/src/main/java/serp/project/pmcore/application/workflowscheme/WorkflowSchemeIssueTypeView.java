/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public record WorkflowSchemeIssueTypeView(
        Long id,
        Long tenantId,
        String typeKey,
        String name,
        Integer hierarchyLevel,
        boolean isSystem,
        boolean readOnly
) {
    public static WorkflowSchemeIssueTypeView from(IssueTypeEntity entity) {
        return new WorkflowSchemeIssueTypeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getTypeKey(),
                entity.getName(),
                entity.getHierarchyLevel(),
                entity.isSystem(),
                entity.isSystem()
        );
    }
}
