/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.update;

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
public class UpdateProjectRoleCommandHandler implements ICommandHandler<UpdateProjectRoleCommand, ProjectRoleView> {

    private final IProjectRoleService projectRoleService;
    private final ProjectRoleOutboxPublisher projectRoleOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectRoleView handle(UpdateProjectRoleCommand command) {
        ProjectRoleEntity updated = projectRoleService.updateProjectRole(
                command.roleId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        projectRoleOutboxPublisher.publishUpdated(
                command.tenantId(),
                ProjectRoleEventPayload.from(updated, command.userId())
        );
        return ProjectRoleView.from(updated, false);
    }
}
