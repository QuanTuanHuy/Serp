/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListWorkItemTimelineQueryHandler implements IQueryHandler<ListWorkItemTimelineQuery, WorkItemTimelinePageView> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IWorkItemPlanPort workItemPlanPort;

    @Override
    @Transactional(readOnly = true)
    public WorkItemTimelinePageView handle(ListWorkItemTimelineQuery query) {
        ProjectEntity project = projectService.getProjectById(query.criteria().getProjectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        PageResult<WorkItemTimelineItemProjection> itemsResult = workItemReadPort.listTimelineWorkItems(query.tenantId(), query.criteria());
        List<Long> workItemIds = itemsResult.items().stream()
                .map(WorkItemTimelineItemProjection::id)
                .toList();
        Map<Long, WorkItemPlanEntity> plansByWorkItemId = workItemPlanPort
                .listActivePlansByWorkItemIds(query.tenantId(), workItemIds)
                .stream()
                .collect(Collectors.toMap(WorkItemPlanEntity::getWorkItemId, Function.identity(), (left, right) -> left));
        List<WorkItemTimelineItemView> items = itemsResult.items().stream()
                .map(item -> WorkItemTimelineItemView.from(item, plansByWorkItemId.get(item.id())))
                .toList();

        List<WorkItemTimelineDependencyView> dependencies = query.includeDependencies()
                ? workItemReadPort.listTimelineDependencies(query.tenantId(), query.criteria().getProjectId(), workItemIds).stream()
                        .map(WorkItemTimelineDependencyView::from)
                        .toList()
                : List.of();

        return new WorkItemTimelinePageView(
                items,
                dependencies,
                itemsResult.total(),
                PageViews.totalPages(itemsResult.total(), query.criteria().getPageSize()),
                query.criteria().getPage(),
                query.criteria().getPageSize()
        );
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
