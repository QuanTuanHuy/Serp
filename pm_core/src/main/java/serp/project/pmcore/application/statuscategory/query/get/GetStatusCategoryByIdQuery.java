/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;

public record GetStatusCategoryByIdQuery(
        Long statusCategoryId,
        Long tenantId
) implements IQuery<StatusCategoryView> {
}
