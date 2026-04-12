/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetProjectByIdQuery(
        Long projectId,
        Long tenantId,
        Set<ProjectExpandOption> expand
) implements IQuery<ProjectDetailView> {

    public GetProjectByIdQuery {
        expand = expand == null ? Set.of() : Set.copyOf(expand);
    }

    public GetProjectByIdQuery(Long projectId, Long tenantId) {
        this(projectId, tenantId, Set.of());
    }
}
