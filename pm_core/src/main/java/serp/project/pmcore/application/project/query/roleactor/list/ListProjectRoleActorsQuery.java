/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.roleactor.list;

import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.List;
import java.util.Set;

public record ListProjectRoleActorsQuery(
        Long projectId,
        Long roleId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<List<ProjectRoleActorView>> {
    public ListProjectRoleActorsQuery {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
