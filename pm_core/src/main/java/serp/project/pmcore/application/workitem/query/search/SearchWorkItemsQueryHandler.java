/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchWorkItemsQueryHandler implements IQueryHandler<SearchWorkItemsQuery, PageView<WorkItemSearchView>> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectReadPort projectReadPort;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IUserProfileClient userProfileClient;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkItemSearchView> handle(SearchWorkItemsQuery query) {
        WorkItemSearchCriteria criteria = query.criteria();
        ProjectEntity project = projectReadPort.getProjectById(criteria.getProjectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(criteria.getProjectId()));

        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        PageResult<WorkItemEntity> workItems = workItemReadPort.searchWorkItems(query.tenantId(), criteria);
        Map<Long, WorkItemSearchView.UserSummary> userSummaryMap = resolveUserSummaries(workItems.items());
        PageResult<WorkItemSearchView> result = workItems.map(workItem -> WorkItemSearchView.from(
                workItem,
                nullableMapGet(userSummaryMap, workItem.getAssigneeId()),
                nullableMapGet(userSummaryMap, workItem.getReporterId())
        ));

        return PageViews.from(result, criteria);
    }

    private Map<Long, WorkItemSearchView.UserSummary> resolveUserSummaries(List<WorkItemEntity> workItems) {
        List<Long> userIds = workItems.stream()
                .flatMap(workItem -> Stream.of(workItem.getAssigneeId(), workItem.getReporterId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userProfileClient.getUserProfilesByIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(profile -> profile.getId() != null)
                .collect(Collectors.toMap(
                        UserProfileDto::getId,
                        this::toUserSummary,
                        (left, right) -> left
                ));
    }

    private WorkItemSearchView.UserSummary toUserSummary(UserProfileDto profile) {
        String displayName = profile.getFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = profile.getEmail();
        }
        return new WorkItemSearchView.UserSummary(profile.getId(), displayName, profile.getAvatarUrl());
    }

    private static WorkItemSearchView.UserSummary nullableMapGet(Map<Long, WorkItemSearchView.UserSummary> map, Long key) {
        return key == null ? null : map.get(key);
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
