/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.unarchive;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.command.update.UpdateProjectResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;

@Service
@RequiredArgsConstructor
public class UnarchiveProjectCommandHandler implements ICommandHandler<UnarchiveProjectCommand, UpdateProjectResult> {

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateProjectResult handle(UnarchiveProjectCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());

        ProjectPermissionEvaluationContext context = ProjectPermissionEvaluationContext.builder()
                .userId(command.userId())
                .groupKeys(command.groupKeys())
                .build();
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                context,
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        return UpdateProjectResult.from(
                projectService.unarchiveProject(command.projectId(), command.tenantId(), command.userId())
        );
    }
}
