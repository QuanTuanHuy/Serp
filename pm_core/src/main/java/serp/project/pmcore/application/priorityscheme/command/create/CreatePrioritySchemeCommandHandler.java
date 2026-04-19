/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;

@Service
@RequiredArgsConstructor
public class CreatePrioritySchemeCommandHandler implements ICommandHandler<CreatePrioritySchemeCommand, PrioritySchemeView> {

    private final IPrioritySchemeService prioritySchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrioritySchemeView handle(CreatePrioritySchemeCommand command) {
        PrioritySchemeEntity created = prioritySchemeService.createPriorityScheme(
                PrioritySchemeEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .defaultPriorityId(command.defaultPriorityId())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return PrioritySchemeView.from(created, false);
    }
}
