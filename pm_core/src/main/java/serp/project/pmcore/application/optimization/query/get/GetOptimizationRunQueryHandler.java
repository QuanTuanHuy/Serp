/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

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
public class GetOptimizationRunQueryHandler implements IQueryHandler<GetOptimizationRunQuery, OptimizationRunReviewView> {
    private final OptimizationRunGuard optimizationRunGuard;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    private final IWorkItemReadPort workItemReadPort;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public OptimizationRunReviewView handle(GetOptimizationRunQuery query) {
        OptimizationRunEntity run = optimizationRunGuard.requireRunInProject(
                query.tenantId(),
                query.projectId(),
                query.runId()
        );
        List<OptimizationRunItemEntity> items = optimizationRunItemPort.listByRunId(query.tenantId(), query.runId());
        return optimizationRunReviewAssembler.toView(
                run,
                items,
                optimizationRunWarningPort.listByRunId(query.tenantId(), query.runId()),
                resolveWorkItems(query, items),
                resolveUsers(items)
        );
    }

    private Map<Long, OptimizationWorkItemSummaryView> resolveWorkItems(GetOptimizationRunQuery query,
                                                                        List<OptimizationRunItemEntity> items) {
        List<Long> workItemIds = items.stream()
                .map(OptimizationRunItemEntity::getWorkItemId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (workItemIds.isEmpty()) {
            return Map.of();
        }
        return workItemReadPort.listActiveByWorkItemIds(query.tenantId(), workItemIds)
                .stream()
                .filter(workItem -> Objects.equals(workItem.getProjectId(), query.projectId()))
                .filter(workItem -> workItem.getId() != null)
                .collect(Collectors.toMap(
                        WorkItemEntity::getId,
                        this::toWorkItemSummary,
                        (left, right) -> left
                ));
    }

    private OptimizationWorkItemSummaryView toWorkItemSummary(WorkItemEntity workItem) {
        return new OptimizationWorkItemSummaryView(
                workItem.getId(),
                workItem.getKey(),
                workItem.getSummary(),
                workItem.getIssueTypeName(),
                workItem.getStatusName(),
                workItem.getPriorityName()
        );
    }

    private Map<Long, UserSummary> resolveUsers(List<OptimizationRunItemEntity> items) {
        Set<Long> userIds = items.stream()
                .flatMap(item -> Stream.of(
                        item.getCurrentAssigneeId(),
                        item.getSuggestedAssigneeId(),
                        item.getOverrideAssigneeId()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userService.getUserProfilesByIds(List.copyOf(userIds))
                .stream()
                .filter(Objects::nonNull)
                .map(UserSummary::from)
                .filter(Objects::nonNull)
                .filter(user -> user.id() != null)
                .collect(Collectors.toMap(
                        UserSummary::id,
                        Function.identity(),
                        (left, right) -> left
                ));
    }
}
