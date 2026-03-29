/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;

public interface IProjectRoleActorPort {
    ProjectRoleActorEntity saveProjectRoleActor(ProjectRoleActorEntity actor);

    Optional<ProjectRoleActorEntity> findActiveAssignment(Long tenantId,
                                                          Long projectId,
                                                          Long projectRoleId,
                                                          String subjectType,
                                                          String subjectId);

    boolean existsActiveAssignment(Long tenantId,
                                   Long projectId,
                                   Long projectRoleId,
                                   String subjectType,
                                   String subjectId);

    int softDeleteActiveAssignment(Long tenantId,
                                   Long projectId,
                                   Long projectRoleId,
                                   String subjectType,
                                   String subjectId,
                                   Long updatedBy);

    List<ProjectRoleActorEntity> getProjectRoleActorsByProjectIdAndRoleId(Long projectId,
                                                                          Long projectRoleId,
                                                                          Long tenantId);
}
