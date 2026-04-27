/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.status.StatusView;

public record CreateStatusCommand(
        String statusKey,
        String name,
        String description,
        String iconUrl,
        Long statusCategoryId,
        Long tenantId,
        Long userId
) implements ICommand<StatusView> {
}
