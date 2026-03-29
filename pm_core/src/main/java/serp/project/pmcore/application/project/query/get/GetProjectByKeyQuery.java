/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetProjectByKeyQuery(
        String key,
        Long tenantId,
        Set<ProjectExpandOption> expand
) implements IQuery<ProjectDetailView> {

    public GetProjectByKeyQuery {
        expand = expand == null ? Set.of() : Set.copyOf(expand);
    }

    public GetProjectByKeyQuery(String key, Long tenantId) {
        this(key, tenantId, Set.of());
    }
}
