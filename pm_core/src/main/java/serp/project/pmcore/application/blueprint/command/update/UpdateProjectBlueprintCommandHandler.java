/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;

@Service
@RequiredArgsConstructor
public class UpdateProjectBlueprintCommandHandler implements ICommandHandler<UpdateProjectBlueprintCommand, ProjectBlueprintView> {

    private final IProjectBlueprintService projectBlueprintService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectBlueprintView handle(UpdateProjectBlueprintCommand command) {
        ProjectBlueprintEntity updated = projectBlueprintService.updateBlueprint(
                command.blueprintId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return ProjectBlueprintView.from(updated);
    }
}
