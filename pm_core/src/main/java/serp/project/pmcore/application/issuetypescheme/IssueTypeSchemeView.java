/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;

public record IssueTypeSchemeView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultIssueTypeId,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static IssueTypeSchemeView from(IssueTypeSchemeEntity entity) {
        return from(entity, entity.isSystem());
    }

    public static IssueTypeSchemeView from(IssueTypeSchemeEntity entity, boolean readOnly) {
        return new IssueTypeSchemeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultIssueTypeId(),
                entity.isSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
