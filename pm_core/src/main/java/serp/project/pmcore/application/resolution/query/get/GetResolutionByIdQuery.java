/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.query.get;

import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetResolutionByIdQuery(
        Long id,
        Long tenantId
) implements IQuery<ResolutionView> {
}
