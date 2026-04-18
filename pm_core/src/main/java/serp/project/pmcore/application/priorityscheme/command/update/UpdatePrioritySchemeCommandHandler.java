/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;

@Service
@RequiredArgsConstructor
public class UpdatePrioritySchemeCommandHandler implements ICommandHandler<UpdatePrioritySchemeCommand, PrioritySchemeView> {

    private final IPrioritySchemeService prioritySchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrioritySchemeView handle(UpdatePrioritySchemeCommand command) {
        PrioritySchemeEntity updated = prioritySchemeService.updatePriorityScheme(
                command.schemeId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return PrioritySchemeView.from(updated, false);
    }
}
