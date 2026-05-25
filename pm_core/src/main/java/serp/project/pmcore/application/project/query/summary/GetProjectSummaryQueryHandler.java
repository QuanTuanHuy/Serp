/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProjectSummaryQueryHandler implements IQueryHandler<GetProjectSummaryQuery, ProjectSummaryView> {

    private static final long SUMMARY_WINDOW_MILLIS = TimeUnit.DAYS.toMillis(7);

    private final IProjectReadPort projectReadPort;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IWorkItemReadPort workItemReadPort;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public ProjectSummaryView handle(GetProjectSummaryQuery query) {
        ProjectSummaryCriteria criteria = query.criteria();
        ProjectEntity project = projectReadPort.getProjectById(criteria.getProjectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(criteria.getProjectId()));

        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - SUMMARY_WINDOW_MILLIS;
        long sevenDaysAhead = now + SUMMARY_WINDOW_MILLIS;

        ProjectSummaryMetricsView metrics = ProjectSummaryMetricsView.from(
                workItemReadPort.getProjectSummaryMetrics(query.tenantId(), criteria, now, sevenDaysAgo, sevenDaysAhead)
        );
        List<ProjectSummaryBreakdownItemView> statuses = workItemReadPort
                .listProjectSummaryStatuses(query.tenantId(), criteria)
                .stream()
                .map(ProjectSummaryBreakdownItemView::from)
                .toList();
        List<ProjectSummaryBreakdownItemView> priorities = workItemReadPort
                .listProjectSummaryPriorities(query.tenantId(), criteria)
                .stream()
                .map(ProjectSummaryBreakdownItemView::from)
                .toList();
        List<ProjectSummaryBreakdownItemView> issueTypes = workItemReadPort
                .listProjectSummaryIssueTypes(query.tenantId(), criteria)
                .stream()
                .map(ProjectSummaryBreakdownItemView::from)
                .toList();
        PageResult<ProjectSummaryActivityProjection> activityResult =
                workItemReadPort.listProjectSummaryActivities(query.tenantId(), criteria);
        List<Long> assigneeIds = workItemReadPort.listProjectSummaryAssigneeIds(query.tenantId(), criteria.getProjectId());
        Map<Long, UserSummary> usersById = resolveUserSummaries(
                activityResult.items(),
                assigneeIds
        );
        PageView<ProjectSummaryActivityView> recentActivity = toActivityPage(activityResult, criteria, usersById);

        ProjectSummaryFilterOptionsView filterOptions = new ProjectSummaryFilterOptionsView(
                buildAssigneeOptions(usersById, assigneeIds),
                workItemReadPort.listProjectSummaryParentOptions(query.tenantId(), criteria.getProjectId())
                        .stream()
                        .map(ProjectSummaryParentOptionView::from)
                        .toList(),
                priorities,
                statuses,
                issueTypes
        );

        return new ProjectSummaryView(
                criteria.getProjectId(),
                metrics,
                ProjectSummaryStatusOverviewView.from(statuses),
                priorities,
                issueTypes,
                recentActivity,
                filterOptions
        );
    }

    private Map<Long, UserSummary> resolveUserSummaries(List<ProjectSummaryActivityProjection> activities,
                                                        List<Long> assigneeIds) {
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        activities.stream()
                .map(ProjectSummaryActivityProjection::actorId)
                .filter(Objects::nonNull)
                .forEach(userIds::add);
        assigneeIds.stream()
                .filter(Objects::nonNull)
                .forEach(userIds::add);
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userService.getUserProfilesByIds(List.copyOf(userIds)).stream()
                .filter(Objects::nonNull)
                .filter(profile -> profile.getId() != null)
                .map(UserSummary::from)
                .collect(Collectors.toMap(
                        UserSummary::id,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private List<UserSummary> buildAssigneeOptions(Map<Long, UserSummary> usersById, List<Long> assigneeIds) {
        return assigneeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(userId -> usersById.getOrDefault(userId, UserSummary.missing(userId)))
                .toList();
    }

    private PageView<ProjectSummaryActivityView> toActivityPage(PageResult<ProjectSummaryActivityProjection> result,
                                                                ProjectSummaryCriteria criteria,
                                                                Map<Long, UserSummary> usersById) {
        List<ProjectSummaryActivityView> items = result.items().stream()
                .map(activity -> ProjectSummaryActivityView.from(
                        activity,
                        activity.actorId() == null ? null : usersById.getOrDefault(activity.actorId(), UserSummary.missing(activity.actorId()))
                ))
                .toList();

        return new PageView<>(
                items,
                result.total(),
                PageViews.totalPages(result.total(), criteria.getActivitySize()),
                criteria.getActivityPage(),
                criteria.getActivitySize()
        );
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
