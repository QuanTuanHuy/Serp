/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.get;

import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetProjectRoleByIdQuery(
        Long roleId,
        Long tenantId
) implements IQuery<ProjectRoleView> {
}
