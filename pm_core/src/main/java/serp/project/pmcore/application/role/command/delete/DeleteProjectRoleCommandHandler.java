/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.role.command.ProjectRoleEventPayload;
import serp.project.pmcore.application.role.command.ProjectRoleOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleService;

@Service
@RequiredArgsConstructor
public class DeleteProjectRoleCommandHandler implements ICommandHandler<DeleteProjectRoleCommand, DeleteProjectRoleResult> {

    private final IProjectRoleService projectRoleService;
    private final ProjectRoleOutboxPublisher projectRoleOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteProjectRoleResult handle(DeleteProjectRoleCommand command) {
        ProjectRoleEntity deleted = projectRoleService.deleteProjectRole(
                command.roleId(),
                command.tenantId(),
                command.userId()
        );
        projectRoleOutboxPublisher.publishDeleted(
                command.tenantId(),
                ProjectRoleEventPayload.from(deleted, command.userId())
        );
        return new DeleteProjectRoleResult(
                deleted.getId(),
                true,
                deleted.getDeletedAt(),
                deleted.getUpdatedBy()
        );
    }
}
