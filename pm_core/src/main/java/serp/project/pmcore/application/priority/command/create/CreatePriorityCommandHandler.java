/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.create;

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
public class CreatePriorityCommandHandler implements ICommandHandler<CreatePriorityCommand, PriorityView> {

    private final IPriorityService priorityService;
    private final PriorityOutboxPublisher priorityOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PriorityView handle(CreatePriorityCommand command) {
        PriorityEntity created = priorityService.createPriority(
                PriorityEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .iconUrl(command.iconUrl())
                        .color(command.color())
                        .sequence(command.sequence())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        priorityOutboxPublisher.publishPriorityCreated(
                command.tenantId(),
                PriorityEventPayload.from(created, command.userId())
        );
        return PriorityView.from(created, false);
    }
}
