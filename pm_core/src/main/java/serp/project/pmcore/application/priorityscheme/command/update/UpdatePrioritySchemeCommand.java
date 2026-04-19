/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.update;

import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;

public record UpdatePrioritySchemeCommand(
        Long schemeId,
        PrioritySchemeUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<PrioritySchemeView> {
}
