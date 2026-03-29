/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.add;

import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;

public record AddProjectRoleActorResult(
        Long id,
        Long projectId,
        Long projectRoleId,
        String subjectType,
        String subjectId,
        Long createdAt,
        Long createdBy
) {
    public static AddProjectRoleActorResult from(ProjectRoleActorEntity entity) {
        return new AddProjectRoleActorResult(
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
