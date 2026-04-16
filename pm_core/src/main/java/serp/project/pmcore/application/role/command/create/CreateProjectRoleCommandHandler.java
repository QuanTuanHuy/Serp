/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.role.command.ProjectRoleEventPayload;
import serp.project.pmcore.application.role.command.ProjectRoleOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleService;

@Service
@RequiredArgsConstructor
public class CreateProjectRoleCommandHandler implements ICommandHandler<CreateProjectRoleCommand, ProjectRoleView> {

    private final IProjectRoleService projectRoleService;
    private final ProjectRoleOutboxPublisher projectRoleOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectRoleView handle(CreateProjectRoleCommand command) {
        ProjectRoleEntity created = projectRoleService.createProjectRole(
                ProjectRoleEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        projectRoleOutboxPublisher.publishCreated(
                command.tenantId(),
                ProjectRoleEventPayload.from(created, command.userId())
        );
        return ProjectRoleView.from(created, false);
    }
}
