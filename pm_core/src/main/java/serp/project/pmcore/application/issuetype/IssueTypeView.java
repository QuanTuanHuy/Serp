/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public record IssueTypeView(
        Long id,
        Long tenantId,
        String typeKey,
        String name,
        String description,
        String iconUrl,
        Integer hierarchyLevel,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static IssueTypeView from(IssueTypeEntity entity) {
        return from(entity, entity.isSystem());
    }

    public static IssueTypeView from(IssueTypeEntity entity, boolean readOnly) {
        return new IssueTypeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getTypeKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getIconUrl(),
                entity.getHierarchyLevel(),
                entity.isSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
