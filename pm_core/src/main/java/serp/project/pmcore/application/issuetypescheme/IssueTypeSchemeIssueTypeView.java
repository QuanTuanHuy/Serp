/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public record IssueTypeSchemeIssueTypeView(
        Long id,
        Long tenantId,
        String typeKey,
        String name,
        Integer hierarchyLevel,
        boolean isSystem,
        boolean readOnly
) {
    public static IssueTypeSchemeIssueTypeView from(IssueTypeEntity entity) {
        return new IssueTypeSchemeIssueTypeView(
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
