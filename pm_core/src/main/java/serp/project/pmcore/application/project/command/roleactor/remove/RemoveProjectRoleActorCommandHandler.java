/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.remove;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RemoveProjectRoleActorCommandHandler
        implements ICommandHandler<RemoveProjectRoleActorCommand, Unit> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Unit handle(RemoveProjectRoleActorCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        projectPermissionEvaluationService.checkPermission(
                project,
                buildEvaluationContext(command.userId(), command.groupKeys()),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        projectRoleService.getProjectRoleByIdIncludingSystem(command.roleId(), command.tenantId());
        projectRoleActorService.removeActor(
                command.tenantId(),
                command.projectId(),
                command.roleId(),
                ProjectRoleActorSubjectType.fromValue(command.subjectType()).name(),
                command.subjectId(),
                command.userId()
        );
        return Unit.VALUE;
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();
    }
}
