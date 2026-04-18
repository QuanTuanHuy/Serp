/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
public class UpdateStatusCommandHandler implements ICommandHandler<UpdateStatusCommand, StatusView> {

    private final IStatusService statusService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusView handle(UpdateStatusCommand command) {
        StatusEntity updated = statusService.updateStatus(
                command.statusId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return StatusView.from(updated, false);
    }
}
