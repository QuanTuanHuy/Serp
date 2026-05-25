/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.query.get;

import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetProjectComponentByIdQuery(
        Long projectId,
        Long componentId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<ProjectComponentView> {
    public GetProjectComponentByIdQuery {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
