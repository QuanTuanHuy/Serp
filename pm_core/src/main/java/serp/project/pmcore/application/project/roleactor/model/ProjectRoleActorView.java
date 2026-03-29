/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.roleactor.model;

import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;

public record ProjectRoleActorView(
        Long id,
        Long projectId,
        Long projectRoleId,
        String subjectType,
        String subjectId,
        Long createdAt,
        Long createdBy
) {
    public static ProjectRoleActorView from(ProjectRoleActorEntity entity) {
        return new ProjectRoleActorView(
                entity.getId(),
                entity.getProjectId(),
                entity.getProjectRoleId(),
                entity.getSubjectType(),
                entity.getSubjectId(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }
}
