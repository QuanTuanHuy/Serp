/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
public class CreateResolutionCommandHandler implements ICommandHandler<CreateResolutionCommand, ResolutionView> {

    private final IResolutionService resolutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResolutionView handle(CreateResolutionCommand command) {
        ResolutionEntity created = resolutionService.createResolution(
                ResolutionEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .sequence(command.sequence())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return ResolutionView.from(created, false);
    }
}
