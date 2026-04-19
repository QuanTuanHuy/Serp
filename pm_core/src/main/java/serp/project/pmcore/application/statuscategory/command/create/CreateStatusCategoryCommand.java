/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;

public record CreateStatusCategoryCommand(
        String name,
        String key,
        String color,
        Long tenantId,
        Long userId
) implements ICommand<StatusCategoryView> {
}
