/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
public class UpdateResolutionCommandHandler implements ICommandHandler<UpdateResolutionCommand, ResolutionView> {

    private final IResolutionService resolutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResolutionView handle(UpdateResolutionCommand command) {
        ResolutionEntity updated = resolutionService.updateResolution(
                command.id(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return ResolutionView.from(updated, false);
    }
}
