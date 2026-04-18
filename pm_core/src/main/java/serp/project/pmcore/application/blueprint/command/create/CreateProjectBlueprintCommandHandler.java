/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;

@Service
@RequiredArgsConstructor
public class CreateProjectBlueprintCommandHandler implements ICommandHandler<CreateProjectBlueprintCommand, ProjectBlueprintView> {

    private final IProjectBlueprintService projectBlueprintService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectBlueprintView handle(CreateProjectBlueprintCommand command) {
        ProjectBlueprintEntity created = projectBlueprintService.createBlueprint(
                ProjectBlueprintEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .typeKey(command.projectTypeKey())
                        .avatarUrl(command.avatarUrl())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return ProjectBlueprintView.from(created);
    }
}
