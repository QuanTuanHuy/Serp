/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
public class DeleteStatusCommandHandler implements ICommandHandler<DeleteStatusCommand, DeleteStatusResult> {

    private final IStatusService statusService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteStatusResult handle(DeleteStatusCommand command) {
        StatusEntity deleted = statusService.deleteStatus(
                command.statusId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteStatusResult.from(deleted);
    }
}
