/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;

@Service
@RequiredArgsConstructor
public class ListProjectComponentsQueryHandler
        implements IQueryHandler<ListProjectComponentsQuery, PageView<ProjectComponentView>> {

    private final IProjectService projectService;
    private final IProjectComponentService projectComponentService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectComponentView> handle(ListProjectComponentsQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(query.userId())
                        .groupKeys(query.groupKeys())
                        .build(),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        ProjectComponentListCriteria criteria = query.toCriteria();
        return PageViews.from(
                projectComponentService.listComponents(query.projectId(), query.tenantId(), criteria),
                criteria,
                ProjectComponentView::from
        );
    }
}
