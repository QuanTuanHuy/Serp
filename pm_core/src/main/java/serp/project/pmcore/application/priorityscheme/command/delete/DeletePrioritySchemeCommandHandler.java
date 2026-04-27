/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;

@Service
@RequiredArgsConstructor
public class DeletePrioritySchemeCommandHandler implements ICommandHandler<DeletePrioritySchemeCommand, DeletePrioritySchemeResult> {

    private final IPrioritySchemeService prioritySchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeletePrioritySchemeResult handle(DeletePrioritySchemeCommand command) {
        PrioritySchemeEntity deleted = prioritySchemeService.deletePriorityScheme(
                command.schemeId(),
                command.tenantId(),
                command.userId()
        );
        return DeletePrioritySchemeResult.from(deleted);
    }
}
