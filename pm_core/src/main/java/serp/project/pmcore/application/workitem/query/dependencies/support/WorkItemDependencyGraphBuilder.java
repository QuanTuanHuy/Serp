/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies.support;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.query.dependencies.WorkItemDependencyEdgeView;
import serp.project.pmcore.application.workitem.query.dependencies.WorkItemDependencyNodeView;
import serp.project.pmcore.application.workitem.query.dependencies.WorkItemDependencySummaryView;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemDependencyCriteria;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WorkItemDependencyGraphBuilder {

    public GraphBuildResult build(WorkItemDependencyCriteria criteria,
                                  List<WorkItemEntity> focusedItems,
                                  Map<Long, WorkItemEntity> itemsById,
                                  List<IssueLinkDetailEntity> links,
                                  Map<Long, WorkItemPlanEntity> plansByWorkItemId) {
        Set<Long> focusedIds = focusedItems.stream()
                .map(WorkItemEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, WorkItemDependencyEdgeView> edgesById = new LinkedHashMap<>();

        for (IssueLinkDetailEntity link : links) {
            if (link.getLinkId() == null || link.getSourceId() == null || link.getTargetId() == null) {
                continue;
            }
            IssueLinkDependencyBehavior behavior = dependencyBehavior(link);
            boolean relatedLink = behavior == IssueLinkDependencyBehavior.NONE;
            if (relatedLink && !criteria.isIncludeRelatedLinks()) {
                continue;
            }
            if (!itemsById.containsKey(link.getSourceId()) || !itemsById.containsKey(link.getTargetId())) {
                continue;
            }
            boolean outsideFilter = !focusedIds.contains(link.getSourceId()) || !focusedIds.contains(link.getTargetId());
            if (outsideFilter && !criteria.isIncludeOutside()) {
                continue;
            }
            NormalizedDirection direction = normalize(link, behavior);
            boolean externalProject = isExternalProject(criteria.getProjectId(), itemsById.get(link.getSourceId()))
                    || isExternalProject(criteria.getProjectId(), itemsById.get(link.getTargetId()));
            edgesById.putIfAbsent(link.getLinkId(), new WorkItemDependencyEdgeView(
                    link.getLinkId(),
                    link.getSourceId(),
                    link.getTargetId(),
                    direction.predecessorId(),
                    direction.successorId(),
                    link.getLinkTypeId(),
                    link.getLinkTypeName(),
                    behavior.name(),
                    outsideFilter,
                    externalProject,
                    relatedLink,
                    false
            ));
        }

        CycleResult cycleResult = detectCycles(edgesById.values().stream()
                .filter(edge -> !edge.relatedLink())
                .toList());
        List<WorkItemDependencyEdgeView> edges = edgesById.values().stream()
                .map(edge -> new WorkItemDependencyEdgeView(
                        edge.linkId(),
                        edge.sourceId(),
                        edge.targetId(),
                        edge.predecessorId(),
                        edge.successorId(),
                        edge.linkTypeId(),
                        edge.linkTypeName(),
                        edge.dependencyBehavior(),
                        edge.outsideFilter(),
                        edge.externalProject(),
                        edge.relatedLink(),
                        cycleResult.cycleEdgeIds().contains(edge.linkId())
                ))
                .toList();

        Map<Long, Long> blockedByCount = edges.stream()
                .filter(edge -> !edge.relatedLink())
                .collect(Collectors.groupingBy(WorkItemDependencyEdgeView::successorId, Collectors.counting()));
        Map<Long, Long> blocksCount = edges.stream()
                .filter(edge -> !edge.relatedLink())
                .collect(Collectors.groupingBy(WorkItemDependencyEdgeView::predecessorId, Collectors.counting()));
        Set<Long> nodeIds = edges.stream()
                .flatMap(edge -> List.of(edge.sourceId(), edge.targetId()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        nodeIds.addAll(focusedIds);

        List<WorkItemDependencyNodeView> nodes = nodeIds.stream()
                .map(itemsById::get)
                .filter(Objects::nonNull)
                .map(item -> {
                    WorkItemPlanEntity plan = plansByWorkItemId.get(item.getId());
                    return new WorkItemDependencyNodeView(
                            item.getId(),
                            item.getProjectId(),
                            item.getKey(),
                            item.getSummary(),
                            item.getStatusId(),
                            item.getStatusName(),
                            item.getIssueTypeId(),
                            item.getIssueTypeName(),
                            item.getPriorityId(),
                            item.getPriorityName(),
                            item.getAssigneeId(),
                            null,
                            item.getDueDate(),
                            plan == null ? null : plan.getPlannedStart(),
                            plan == null ? null : plan.getPlannedEnd(),
                            !focusedIds.contains(item.getId()),
                            blockedByCount.getOrDefault(item.getId(), 0L).intValue(),
                            blocksCount.getOrDefault(item.getId(), 0L).intValue(),
                            cycleResult.cycleNodeIds().contains(item.getId())
                    );
                })
                .toList();

        long dependencyCount = edges.stream().filter(edge -> !edge.relatedLink()).count();
        WorkItemDependencySummaryView summary = new WorkItemDependencySummaryView(
                nodes.size(),
                dependencyCount,
                edges.stream().filter(edge -> !edge.relatedLink() && edge.outsideFilter()).count(),
                blocksCount.keySet().size(),
                blockedByCount.keySet().size(),
                edges.stream().filter(WorkItemDependencyEdgeView::relatedLink).count(),
                cycleResult.cycleEdgeIds().size()
        );
        return new GraphBuildResult(nodes, edges, summary);
    }

    private IssueLinkDependencyBehavior dependencyBehavior(IssueLinkDetailEntity link) {
        return link.getDependencyBehavior() == null
                ? IssueLinkDependencyBehavior.NONE
                : link.getDependencyBehavior();
    }

    private NormalizedDirection normalize(IssueLinkDetailEntity link, IssueLinkDependencyBehavior behavior) {
        if (behavior == IssueLinkDependencyBehavior.SOURCE_DEPENDS_ON_TARGET) {
            return new NormalizedDirection(link.getTargetId(), link.getSourceId());
        }
        return new NormalizedDirection(link.getSourceId(), link.getTargetId());
    }

    private boolean isExternalProject(Long projectId, WorkItemEntity item) {
        return item != null && projectId != null && !projectId.equals(item.getProjectId());
    }

    private CycleResult detectCycles(List<WorkItemDependencyEdgeView> edges) {
        Map<Long, List<WorkItemDependencyEdgeView>> outgoing = edges.stream()
                .collect(Collectors.groupingBy(WorkItemDependencyEdgeView::predecessorId));
        Set<Long> cycleEdgeIds = new HashSet<>();
        Set<Long> cycleNodeIds = new HashSet<>();

        for (WorkItemDependencyEdgeView edge : edges) {
            if (hasPath(edge.successorId(), edge.predecessorId(), outgoing)) {
                cycleEdgeIds.add(edge.linkId());
                cycleNodeIds.add(edge.predecessorId());
                cycleNodeIds.add(edge.successorId());
            }
        }
        return new CycleResult(cycleEdgeIds, cycleNodeIds);
    }

    private boolean hasPath(Long start,
                            Long target,
                            Map<Long, List<WorkItemDependencyEdgeView>> outgoing) {
        ArrayDeque<Long> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            if (Objects.equals(current, target)) {
                return true;
            }
            for (WorkItemDependencyEdgeView edge : outgoing.getOrDefault(current, List.of())) {
                queue.add(edge.successorId());
            }
        }
        return false;
    }

    public record GraphBuildResult(
            List<WorkItemDependencyNodeView> nodes,
            List<WorkItemDependencyEdgeView> edges,
            WorkItemDependencySummaryView summary
    ) {
    }

    private record NormalizedDirection(Long predecessorId, Long successorId) {
    }

    private record CycleResult(Set<Long> cycleEdgeIds, Set<Long> cycleNodeIds) {
    }
}
