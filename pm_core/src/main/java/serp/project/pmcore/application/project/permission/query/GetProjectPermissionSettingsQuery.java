/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.query;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetProjectPermissionSettingsQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<ProjectPermissionSettingsView> {
    public GetProjectPermissionSettingsQuery {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
