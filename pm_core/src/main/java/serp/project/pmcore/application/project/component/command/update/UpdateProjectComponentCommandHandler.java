/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;

@Service
@RequiredArgsConstructor
public class UpdateProjectComponentCommandHandler
        implements ICommandHandler<UpdateProjectComponentCommand, ProjectComponentView> {

    private final IProjectService projectService;
    private final IProjectComponentService projectComponentService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectComponentView handle(UpdateProjectComponentCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(command.userId())
                        .groupKeys(command.groupKeys())
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        ProjectComponentEntity updated = projectComponentService.updateComponent(
                command.componentId(),
                command.projectId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return ProjectComponentView.from(updated);
    }
}
