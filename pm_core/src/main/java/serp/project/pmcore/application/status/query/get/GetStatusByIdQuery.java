/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.status.StatusView;

public record GetStatusByIdQuery(
        Long statusId,
        Long tenantId
) implements IQuery<StatusView> {
}
