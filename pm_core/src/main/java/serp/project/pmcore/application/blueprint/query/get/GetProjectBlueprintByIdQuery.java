/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.query.get;

import serp.project.pmcore.application.blueprint.ProjectBlueprintDetailView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetProjectBlueprintByIdQuery(
        Long blueprintId,
        Long tenantId
) implements IQuery<ProjectBlueprintDetailView> {
}
