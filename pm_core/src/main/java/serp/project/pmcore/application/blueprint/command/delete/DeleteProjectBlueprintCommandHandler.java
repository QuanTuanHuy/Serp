/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;

@Service
@RequiredArgsConstructor
public class DeleteProjectBlueprintCommandHandler implements ICommandHandler<DeleteProjectBlueprintCommand, DeleteProjectBlueprintResult> {

    private final IProjectBlueprintService projectBlueprintService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteProjectBlueprintResult handle(DeleteProjectBlueprintCommand command) {
        ProjectBlueprintEntity deleted = projectBlueprintService.deleteBlueprint(
                command.blueprintId(),
                command.tenantId(),
                command.userId()
        );
        return new DeleteProjectBlueprintResult(
                deleted.getId(),
                true,
                deleted.getDeletedAt(),
                deleted.getUpdatedBy()
        );
    }
}
