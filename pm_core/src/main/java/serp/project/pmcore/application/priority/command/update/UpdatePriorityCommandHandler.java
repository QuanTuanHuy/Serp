/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.priority.command.PriorityEventPayload;
import serp.project.pmcore.application.priority.command.PriorityOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.service.IPriorityService;

@Service
@RequiredArgsConstructor
public class UpdatePriorityCommandHandler implements ICommandHandler<UpdatePriorityCommand, PriorityView> {

    private final IPriorityService priorityService;
    private final PriorityOutboxPublisher priorityOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PriorityView handle(UpdatePriorityCommand command) {
        PriorityEntity updated = priorityService.updatePriority(
                command.priorityId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        priorityOutboxPublisher.publishPriorityUpdated(
                command.tenantId(),
                PriorityEventPayload.from(updated, command.userId())
        );
        return PriorityView.from(updated, false);
    }
}
