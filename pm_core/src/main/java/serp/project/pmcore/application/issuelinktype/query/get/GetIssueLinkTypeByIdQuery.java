/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.query.get;

import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetIssueLinkTypeByIdQuery(
        Long id,
        Long tenantId
) implements IQuery<IssueLinkTypeView> {
}
