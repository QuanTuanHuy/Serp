/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.blueprint.ProjectBlueprintDetailView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;

@Service
@RequiredArgsConstructor
public class GetProjectBlueprintByIdQueryHandler implements IQueryHandler<GetProjectBlueprintByIdQuery, ProjectBlueprintDetailView> {

    private final IProjectBlueprintService projectBlueprintService;

    @Override
    @Transactional(readOnly = true)
    public ProjectBlueprintDetailView handle(GetProjectBlueprintByIdQuery query) {
        ProjectBlueprintEntity blueprint = projectBlueprintService.getBlueprintByIdIncludingSystemOrThrow(
                query.blueprintId(),
                query.tenantId()
        );
        return ProjectBlueprintDetailView.from(
                blueprint,
                projectBlueprintService.getBlueprintDefaultsIncludingSystem(query.blueprintId(), query.tenantId())
        );
    }
}
