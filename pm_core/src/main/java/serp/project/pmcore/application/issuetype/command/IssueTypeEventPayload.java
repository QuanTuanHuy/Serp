/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public record IssueTypeEventPayload(
        Long issueTypeId,
        String typeKey,
        String name,
        Integer hierarchyLevel,
        boolean isSystem,
        Long performedBy,
        Long deletedAt
) {
    public static IssueTypeEventPayload from(IssueTypeEntity entity, Long performedBy) {
        return new IssueTypeEventPayload(
                entity.getId(),
                entity.getTypeKey(),
                entity.getName(),
                entity.getHierarchyLevel(),
                entity.isSystem(),
                performedBy,
                entity.getDeletedAt()
        );
    }
}
