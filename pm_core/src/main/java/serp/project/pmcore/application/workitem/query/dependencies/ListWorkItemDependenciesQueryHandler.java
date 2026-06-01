/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.workitem.query.dependencies.support.WorkItemDependencyGraphBuilder;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemDependencyCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ListWorkItemDependenciesQueryHandler
        implements IQueryHandler<ListWorkItemDependenciesQuery, WorkItemDependenciesPageView> {

    private final IWorkItemReadPort workItemReadPort;
    private final IIssueLinkPort issueLinkPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IWorkItemPlanPort workItemPlanPort;
    private final WorkItemDependencyGraphBuilder graphBuilder;

    @Override
    @Transactional(readOnly = true)
    public WorkItemDependenciesPageView handle(ListWorkItemDependenciesQuery query) {
        WorkItemDependencyCriteria criteria = query.criteria();
        ProjectEntity project = projectService.getProjectById(criteria.getProjectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        PageResult<WorkItemEntity> focusedResult = workItemReadPort.searchWorkItems(
                query.tenantId(),
                toSearchCriteria(criteria)
        );
        List<WorkItemEntity> focusedItems = focusedResult.items();
        Map<Long, WorkItemEntity> itemsById = focusedItems.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        WorkItemEntity::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        Map<Long, IssueLinkDetailEntity> linksById = new LinkedHashMap<>();
        LinkedHashSet<Long> currentFrontier = focusedItems.stream()
                .map(WorkItemEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<Long> expandedIds = new LinkedHashSet<>();

        for (int level = 0; level < criteria.getEffectiveDepth() && !currentFrontier.isEmpty(); level++) {
            List<Long> frontierIds = new ArrayList<>(currentFrontier);
            expandedIds.addAll(frontierIds);
            List<IssueLinkDetailEntity> links = issueLinkPort.listByWorkItemIds(query.tenantId(), frontierIds);
            links.forEach(link -> {
                if (link.getLinkId() != null) {
                    linksById.putIfAbsent(link.getLinkId(), link);
                }
            });
            if (!criteria.isIncludeOutside()) {
                break;
            }

            List<Long> missingEndpointIds = links.stream()
                    .filter(link -> criteria.isIncludeRelatedLinks()
                            || link.getDependencyBehavior() != IssueLinkDependencyBehavior.NONE)
                    .flatMap(link -> Stream.of(link.getSourceId(), link.getTargetId()))
                    .filter(Objects::nonNull)
                    .filter(id -> !itemsById.containsKey(id))
                    .distinct()
                    .toList();
            if (missingEndpointIds.isEmpty()) {
                currentFrontier = new LinkedHashSet<>();
                continue;
            }

            List<WorkItemEntity> outsideItems = workItemReadPort.listActiveByWorkItemIds(
                    query.tenantId(),
                    missingEndpointIds
            );
            outsideItems.stream()
                    .filter(item -> item.getId() != null)
                    .forEach(item -> itemsById.putIfAbsent(item.getId(), item));
            currentFrontier = outsideItems.stream()
                    .map(WorkItemEntity::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> !expandedIds.contains(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        List<Long> includedIds = new ArrayList<>(itemsById.keySet());
        Map<Long, WorkItemPlanEntity> plansByWorkItemId = workItemPlanPort
                .listActivePlansByWorkItemIds(query.tenantId(), includedIds)
                .stream()
                .filter(plan -> plan.getWorkItemId() != null)
                .collect(Collectors.toMap(
                        WorkItemPlanEntity::getWorkItemId,
                        Function.identity(),
                        (left, right) -> left
                ));
        WorkItemDependencyGraphBuilder.GraphBuildResult graph = graphBuilder.build(
                criteria,
                focusedItems,
                itemsById,
                new ArrayList<>(linksById.values()),
                plansByWorkItemId
        );

        return new WorkItemDependenciesPageView(
                criteria.getProjectId(),
                graph.nodes(),
                graph.edges(),
                graph.summary(),
                focusedResult.total(),
                PageViews.totalPages(focusedResult.total(), criteria.getPageSize()),
                criteria.getPage(),
                criteria.getPageSize(),
                criteria.getEffectiveDepth(),
                criteria.isIncludeOutside(),
                criteria.isIncludeRelatedLinks()
        );
    }

    private WorkItemSearchCriteria toSearchCriteria(WorkItemDependencyCriteria criteria) {
        return WorkItemSearchCriteria.builder()
                .projectId(criteria.getProjectId())
                .keyword(criteria.getKeyword())
                .statusIds(criteria.getStatusIds())
                .assigneeIds(criteria.getAssigneeIds())
                .issueTypeIds(criteria.getIssueTypeIds())
                .priorityIds(criteria.getPriorityIds())
                .parentId(criteria.getParentId())
                .componentIds(criteria.getComponentIds())
                .page(criteria.getPage())
                .pageSize(criteria.getPageSize())
                .enriched(true)
                .build();
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
