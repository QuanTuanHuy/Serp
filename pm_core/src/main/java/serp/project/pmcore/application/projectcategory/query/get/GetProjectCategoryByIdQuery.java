/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.query.get;

import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetProjectCategoryByIdQuery(
        Long categoryId,
        Long tenantId
) implements IQuery<ProjectCategoryView> {
}
