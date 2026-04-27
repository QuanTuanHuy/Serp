/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;

public record UpdateStatusCategoryCommand(
        Long statusCategoryId,
        StatusCategoryUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<StatusCategoryView> {
}
