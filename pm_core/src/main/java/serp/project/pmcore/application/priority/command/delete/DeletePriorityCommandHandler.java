/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priority.command.PriorityEventPayload;
import serp.project.pmcore.application.priority.command.PriorityOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.service.IPriorityService;

@Service
@RequiredArgsConstructor
public class DeletePriorityCommandHandler implements ICommandHandler<DeletePriorityCommand, DeletePriorityResult> {

    private final IPriorityService priorityService;
    private final PriorityOutboxPublisher priorityOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeletePriorityResult handle(DeletePriorityCommand command) {
        PriorityEntity deleted = priorityService.deletePriority(
                command.priorityId(),
                command.tenantId(),
                command.userId()
        );
        priorityOutboxPublisher.publishPriorityDeleted(
                command.tenantId(),
                PriorityEventPayload.from(deleted, command.userId())
        );
        return DeletePriorityResult.from(deleted);
    }
}
