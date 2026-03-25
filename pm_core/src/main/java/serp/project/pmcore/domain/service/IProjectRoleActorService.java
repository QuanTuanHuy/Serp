/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.project.ProjectRoleActorEntity;

import java.util.List;

public interface IProjectRoleActorService {
    ProjectRoleActorEntity assignActor(Long tenantId,
                                       Long projectId,
                                       Long roleId,
                                       String subjectType,
                                       String subjectId,
                                       Long userId);

    ProjectRoleActorEntity assignActorIfAbsent(Long tenantId,
                                               Long projectId,
                                               Long roleId,
                                               String subjectType,
                                               String subjectId,
                                               Long userId);

    void removeActor(Long tenantId,
                     Long projectId,
                     Long roleId,
                     String subjectType,
                     String subjectId,
                     Long userId);

    List<ProjectRoleActorEntity> getActorsByProjectAndRole(Long projectId, Long roleId, Long tenantId);

    boolean hasRoleAssignment(Long tenantId,
                              Long projectId,
                              Long roleId,
                              String subjectType,
                              String subjectId);
}
