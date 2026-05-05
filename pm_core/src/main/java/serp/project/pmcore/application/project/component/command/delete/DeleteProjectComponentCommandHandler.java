/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class DeleteProjectComponentCommandHandler
        implements ICommandHandler<DeleteProjectComponentCommand, DeleteProjectComponentResult> {

    private final IProjectService projectService;
    private final IProjectComponentService projectComponentService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteProjectComponentResult handle(DeleteProjectComponentCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(command.userId())
                        .groupKeys(command.groupKeys())
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        ProjectComponentEntity deleted = projectComponentService.deleteComponent(
                command.componentId(),
                command.projectId(),
                command.tenantId(),
                command.userId()
        );
        return new DeleteProjectComponentResult(
                deleted.getId(),
                true,
                deleted.getDeletedAt(),
                deleted.getUpdatedBy()
        );
    }
}
