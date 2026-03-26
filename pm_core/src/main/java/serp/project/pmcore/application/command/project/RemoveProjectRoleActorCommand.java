/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RemoveProjectRoleActorCommand {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long projectId,
                        Long roleId,
                        String subjectType,
                        String subjectId,
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
        projectRoleActorService.removeActor(
                tenantId,
                projectId,
                roleId,
                ProjectRoleActorSubjectType.fromValue(subjectType).name(),
                subjectId,
                userId
        );
    }
}
