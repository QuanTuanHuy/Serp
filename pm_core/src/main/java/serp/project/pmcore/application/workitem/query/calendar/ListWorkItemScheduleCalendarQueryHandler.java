/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.calendar;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleAllocationCalendarProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListWorkItemScheduleCalendarQueryHandler
        implements IQueryHandler<ListWorkItemScheduleCalendarQuery, WorkItemScheduleCalendarPageView> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public WorkItemScheduleCalendarPageView handle(ListWorkItemScheduleCalendarQuery query) {
        ProjectEntity project = projectService.getProjectById(query.criteria().getProjectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        PageResult<WorkItemScheduleAllocationCalendarProjection> result = workItemReadPort
                .listScheduleAllocationCalendarItems(query.tenantId(), query.criteria());
        Map<Long, UserSummary> assignees = resolveAssigneeSummaries(result.items());
        List<WorkItemScheduleAllocationCalendarItemView> items = result.items().stream()
                .map(item -> applyAssigneeSummary(item, item.assigneeId() == null ? null : assignees.get(item.assigneeId())))
                .map(WorkItemScheduleAllocationCalendarItemView::from)
                .toList();

        return new WorkItemScheduleCalendarPageView(
                items,
                result.total(),
                PageViews.totalPages(result.total(), query.criteria().getPageSize()),
                query.criteria().getPage(),
                query.criteria().getPageSize()
        );
    }

    private Map<Long, UserSummary> resolveAssigneeSummaries(List<WorkItemScheduleAllocationCalendarProjection> items) {
        List<Long> userIds = items.stream()
                .map(WorkItemScheduleAllocationCalendarProjection::assigneeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userService.getUserProfilesByIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(profile -> profile.getId() != null)
                .collect(Collectors.toMap(
                        UserProfileDto::getId,
                        UserSummary::from,
                        (left, right) -> left
                ));
    }

    private WorkItemScheduleAllocationCalendarProjection applyAssigneeSummary(
            WorkItemScheduleAllocationCalendarProjection item,
            UserSummary assignee
    ) {
        if (assignee == null) {
            return item;
        }
        return new WorkItemScheduleAllocationCalendarProjection(
                item.allocationId(),
                item.workItemPlanId(),
                item.workItemId(),
                item.projectId(),
                item.key(),
                item.summary(),
                item.assigneeId(),
                assignee.displayName(),
                assignee.avatarUrl(),
                item.start(),
                item.end(),
                item.effortMillis(),
                item.source(),
                item.sourceRunId(),
                item.sourceRunItemId(),
                item.issueTypeId(),
                item.issueTypeName(),
                item.issueTypeIconUrl(),
                item.issueTypeHierarchyLevel(),
                item.statusId(),
                item.statusName(),
                item.priorityId(),
                item.priorityName(),
                item.priorityColor()
        );
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
