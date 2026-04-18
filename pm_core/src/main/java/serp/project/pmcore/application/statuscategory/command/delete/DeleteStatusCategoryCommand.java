/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteStatusCategoryCommand(
        Long statusCategoryId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteStatusCategoryResult> {
}
