/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.query.WorkItemSearchCriteria;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchWorkItemsQueryHandler implements IQueryHandler<SearchWorkItemsQuery, PageView<WorkItemSearchView>> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectReadPort projectReadPort;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkItemSearchView> handle(SearchWorkItemsQuery query) {
        WorkItemSearchCriteria criteria = query.criteria();
        ProjectEntity project = projectReadPort.getProjectById(criteria.getProjectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(criteria.getProjectId()));

        projectPermissionEvaluationService.checkPermission(
                project,
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        PageResult<WorkItemSearchView> result = workItemReadPort.searchWorkItems(query.tenantId(), criteria)
                .map(WorkItemSearchView::from);

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

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
