/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteProjectCategoryCommand(
        Long categoryId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteProjectCategoryResult> {
}
