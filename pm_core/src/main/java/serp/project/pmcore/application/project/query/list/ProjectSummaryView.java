/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

public record ProjectSummaryView(
        Long id,
        String key,
        String name,
        String description,
        String projectTypeKey,
        Long leadUserId,
        String leadUserName,
        Long avatarId,
        Long categoryId,
        String categoryName,
        Boolean isArchived,
        Long archivedAt,
        Long createdAt,
        Long updatedAt
) {
    public static ProjectSummaryView from(ProjectEntity entity, String leadUserName, String categoryName) {
        return new ProjectSummaryView(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getProjectTypeKey(),
                entity.getLeadUserId(),
                leadUserName,
                entity.getAvatarId(),
                entity.getCategoryId(),
                categoryName,
                entity.getIsArchived(),
                entity.getArchivedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
