/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.create;

import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreatePrioritySchemeCommand(
        String name,
        String description,
        Long defaultPriorityId,
        Long tenantId,
        Long userId
) implements ICommand<PrioritySchemeView> {
}
