/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;

public record UpdateStatusCommand(
        Long statusId,
        StatusUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<StatusView> {
}
