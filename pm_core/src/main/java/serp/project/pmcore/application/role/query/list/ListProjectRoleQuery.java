/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.List;

public record ListProjectRoleQuery(
        Long tenantId
) implements IQuery<List<ProjectRoleView>> {
}
