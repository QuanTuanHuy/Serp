/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.add;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectRoleActorEntity;
import serp.project.pmcore.domain.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AddProjectRoleActorCommandHandler
        implements ICommandHandler<AddProjectRoleActorCommand, ProjectRoleActorView> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectRoleActorView handle(AddProjectRoleActorCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        projectPermissionEvaluationService.checkPermission(
                project,
                buildEvaluationContext(command.userId(), command.groupKeys()),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        projectRoleService.getProjectRoleByIdIncludingSystem(command.roleId(), command.tenantId());
        ProjectRoleActorEntity actor = projectRoleActorService.assignActor(
                command.tenantId(),
                command.projectId(),
                command.roleId(),
                ProjectRoleActorSubjectType.fromValue(command.subjectType()).name(),
                command.subjectId(),
                command.userId()
        );
        return ProjectRoleActorView.from(actor);
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();
    }
}
