/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.update;

import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;

public record UpdateProjectCategoryCommand(
        Long categoryId,
        ProjectCategoryUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<ProjectCategoryView> {
}
