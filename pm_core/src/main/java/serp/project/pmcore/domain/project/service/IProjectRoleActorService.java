/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import java.util.List;

import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;

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

    void removeUserActorsByProject(Long tenantId,
                                   Long projectId,
                                   String subjectId,
                                   Long userId);

    List<ProjectRoleActorEntity> getActorsByProjectAndRole(Long projectId, Long roleId, Long tenantId);

    List<ProjectRoleActorEntity> getActorsByProject(Long projectId, Long tenantId);

    boolean hasRoleAssignment(Long tenantId,
                               Long projectId,
                              Long roleId,
                              String subjectType,
                              String subjectId);
}
