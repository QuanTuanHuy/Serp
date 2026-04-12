/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.update;

import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;

public record UpdatePriorityCommand(
        Long priorityId,
        PriorityUpdateData data,
        boolean priorityKeyProvided,
        boolean tenantIdProvided,
        boolean isSystemProvided,
        Long tenantId,
        Long userId
) implements ICommand<PriorityView> {
}
