/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.query.projectroleactor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.dto.response.project.ProjectRoleActorResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListProjectRoleActorsQuery {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Transactional(readOnly = true)
    public List<ProjectRoleActorResponse> execute(Long projectId,
                                                  Long roleId,
                                                  Long tenantId,
                                                  Long userId,
                                                  Set<String> groupKeys) {
        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        projectPermissionEvaluationService.checkPermission(
                project,
                ProjectPermissionEvaluationContext.builder()
                        .userId(userId)
                        .groupKeys(groupKeys)
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        projectRoleService.getProjectRoleByIdIncludingSystem(roleId, tenantId);
        return projectRoleActorService.getActorsByProjectAndRole(projectId, roleId, tenantId)
                .stream()
                .map(ProjectRoleActorResponse::from)
                .toList();
    }
}
