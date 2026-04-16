/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

@Service
@RequiredArgsConstructor
public class ListProjectRoleQueryHandler implements IQueryHandler<ListProjectRoleQuery, PageView<ProjectRoleView>> {

    private final IProjectRoleService projectRoleService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectRoleView> handle(ListProjectRoleQuery query) {
        ProjectRoleListCriteria criteria = query.toCriteria();
        PageResult<ProjectRoleView> result = projectRoleService.listVisibleProjectRoles(query.tenantId(), criteria)
                .map(role -> ProjectRoleView.from(role, Boolean.TRUE.equals(role.getIsSystem())));
        int pageSize = criteria.getPageSize();
        int currentPage = criteria.getPage();
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) result.total() / pageSize);

        return new PageView<>(
                result.items(),
                result.total(),
                totalPages,
                currentPage,
                pageSize
        );
    }
}
