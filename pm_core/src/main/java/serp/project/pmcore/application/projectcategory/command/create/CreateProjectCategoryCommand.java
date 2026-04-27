/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.create;

import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateProjectCategoryCommand(
        String name,
        String description,
        Long tenantId,
        Long userId
) implements ICommand<ProjectCategoryView> {
}
