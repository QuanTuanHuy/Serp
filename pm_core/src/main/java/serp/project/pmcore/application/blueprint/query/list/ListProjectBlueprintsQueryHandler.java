/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;

@Service
@RequiredArgsConstructor
public class ListProjectBlueprintsQueryHandler implements IQueryHandler<ListProjectBlueprintsQuery, PageView<ProjectBlueprintView>> {

    private final IProjectBlueprintService projectBlueprintService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectBlueprintView> handle(ListProjectBlueprintsQuery query) {
        ProjectBlueprintListCriteria criteria = query.toCriteria();
        return PageViews.from(
                projectBlueprintService.listBlueprintsIncludingSystem(query.tenantId(), criteria),
                criteria,
                ProjectBlueprintView::from
        );
    }
}
