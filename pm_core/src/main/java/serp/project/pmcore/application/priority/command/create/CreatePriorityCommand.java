/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.create;

import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreatePriorityCommand(
        String name,
        String description,
        String iconUrl,
        String color,
        Integer sequence,
        Long tenantId,
        Long userId
) implements ICommand<PriorityView> {
}
