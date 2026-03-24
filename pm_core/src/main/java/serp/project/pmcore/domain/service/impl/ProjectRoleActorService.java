/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.entity.project.ProjectRoleActorEntity;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IProjectRoleActorPort;
import serp.project.pmcore.domain.service.IProjectRoleActorService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectRoleActorService implements IProjectRoleActorService {

    private final IProjectRoleActorPort projectRoleActorPort;

    @Override
    public ProjectRoleActorEntity assignActor(Long tenantId,
                                              Long projectId,
                                              Long roleId,
                                              String subjectType,
                                              String subjectId,
                                              Long userId) {
        if (projectRoleActorPort.existsActiveAssignment(tenantId, projectId, roleId, subjectType, subjectId)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ROLE_ACTOR_ALREADY_ASSIGNED,
                    "Project role actor already assigned: projectId=" + projectId
                            + ", roleId=" + roleId
                            + ", subjectType=" + subjectType
                            + ", subjectId=" + subjectId
            );
        }

        return saveNewActor(tenantId, projectId, roleId, subjectType, subjectId, userId);
    }

    @Override
    public ProjectRoleActorEntity assignActorIfAbsent(Long tenantId,
                                                      Long projectId,
                                                      Long roleId,
                                                      String subjectType,
                                                      String subjectId,
                                                      Long userId) {
        return projectRoleActorPort.findActiveAssignment(tenantId, projectId, roleId, subjectType, subjectId)
                .orElseGet(() -> saveNewActor(tenantId, projectId, roleId, subjectType, subjectId, userId));
    }

    @Override
    public void removeActor(Long tenantId,
                            Long projectId,
                            Long roleId,
                            String subjectType,
                            String subjectId,
                            Long userId) {
        projectRoleActorPort
                .findActiveAssignment(tenantId, projectId, roleId, subjectType, subjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ROLE_ACTOR_NOT_FOUND,
                        "Project role actor not found: projectId=" + projectId
                                + ", roleId=" + roleId
                                + ", subjectType=" + subjectType
                                + ", subjectId=" + subjectId
                ));

        projectRoleActorPort.softDeleteActiveAssignment(
                tenantId,
                projectId,
                roleId,
                subjectType,
                subjectId,
                userId
        );
    }

    @Override
    public List<ProjectRoleActorEntity> getActorsByProjectAndRole(Long projectId, Long roleId, Long tenantId) {
        return projectRoleActorPort.getProjectRoleActorsByProjectIdAndRoleId(projectId, roleId, tenantId);
    }

    @Override
    public boolean hasRoleAssignment(Long tenantId,
                                     Long projectId,
                                     Long roleId,
                                     String subjectType,
                                     String subjectId) {
        return projectRoleActorPort.existsActiveAssignment(tenantId, projectId, roleId, subjectType, subjectId);
    }

    private ProjectRoleActorEntity saveNewActor(Long tenantId,
                                                Long projectId,
                                                Long roleId,
                                                String subjectType,
                                                String subjectId,
                                                Long userId) {
        long now = System.currentTimeMillis();
        ProjectRoleActorEntity actor = ProjectRoleActorEntity.builder()
                .tenantId(tenantId)
                .projectId(projectId)
                .projectRoleId(roleId)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .createdAt(now)
                .createdBy(userId)
                .updatedAt(now)
                .updatedBy(userId)
                .build();
        return projectRoleActorPort.saveProjectRoleActor(actor);
    }
}
