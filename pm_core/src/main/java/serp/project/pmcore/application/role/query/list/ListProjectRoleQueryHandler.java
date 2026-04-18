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
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.project.service.IProjectRoleService;

@Service
@RequiredArgsConstructor
public class ListProjectRoleQueryHandler implements IQueryHandler<ListProjectRoleQuery, PageView<ProjectRoleView>> {

    private final IProjectRoleService projectRoleService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectRoleView> handle(ListProjectRoleQuery query) {
        ProjectRoleListCriteria criteria = query.toCriteria();
        return PageViews.from(
                projectRoleService.listVisibleProjectRoles(query.tenantId(), criteria),
                criteria,
                role -> ProjectRoleView.from(role, Boolean.TRUE.equals(role.getIsSystem()))
        );
    }
}
