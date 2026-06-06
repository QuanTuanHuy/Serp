/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetProjectSettingsOverviewQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<ProjectSettingsOverviewView> {

    public GetProjectSettingsOverviewQuery {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
