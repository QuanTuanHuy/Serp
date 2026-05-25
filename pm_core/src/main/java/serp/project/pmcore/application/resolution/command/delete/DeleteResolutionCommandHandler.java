/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
public class DeleteResolutionCommandHandler implements ICommandHandler<DeleteResolutionCommand, DeleteResolutionResult> {

    private final IResolutionService resolutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteResolutionResult handle(DeleteResolutionCommand command) {
        ResolutionEntity deleted = resolutionService.deleteResolution(
                command.id(),
                command.tenantId(),
                command.userId()
        );
        return DeleteResolutionResult.from(deleted);
    }
}
