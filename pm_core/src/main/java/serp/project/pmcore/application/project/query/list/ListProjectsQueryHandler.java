/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.query.ProjectListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

@Service
@RequiredArgsConstructor
public class ListProjectsQueryHandler implements IQueryHandler<ListProjectsQuery, PageView<ProjectSummaryView>> {

    private final IProjectReadPort projectReadPort;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectSummaryView> handle(ListProjectsQuery query) {
        ProjectListCriteria criteria = query.toCriteria();
        PageResult<ProjectSummaryView> result = projectReadPort.getProjects(
                        query.tenantId(),
                        criteria.getSearch(),
                        criteria.getCategoryId(),
                        criteria.getProjectTypeKey(),
                        criteria.getArchived(),
                        criteria.getPage(),
                        criteria.getPageSize(),
                        criteria.getSortBy(),
                        criteria.getSortDirection()
                )
                .map(ProjectSummaryView::from);

        return PageViews.from(result, criteria);
    }
}
