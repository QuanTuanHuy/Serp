/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetWorkItemByIdQueryHandler implements IQueryHandler<GetWorkItemByIdQuery, WorkItemDetailView> {

    private final IWorkItemReadPort workItemReadPort;

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService permissionEvaluationService;
    private final IUserProfileClient userProfileClient;

    @Override
    @Transactional(readOnly = true)
    public WorkItemDetailView handle(GetWorkItemByIdQuery query) {

        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        var evaluationContext = ProjectPermissionEvaluationContext.builder()
                .userId(query.userId())
                .build();
        permissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                evaluationContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        WorkItemDetailProjection workItem = workItemReadPort
                .getWorkItemDetailById(query.projectId(), query.workItemId(), query.tenantId())
                .orElseThrow(() -> {
                    log.warn("[GetWorkItemByIdQueryHandler] Work item not found: workItemId={}", query.workItemId());
                    return ResourceNotFoundException.workItem(query.workItemId());
                });

        Map<Long, WorkItemDetailView.UserSummaryView> users = resolveUserSummaries(workItem);
        List<WorkItemComponentView> components = workItemReadPort
                .getActiveComponentsByWorkItemId(workItem.getId(), query.tenantId())
                .stream()
                .map(WorkItemComponentView::from)
                .toList();
        WorkItemDetailView.SubtaskStatsView subtaskStats = new WorkItemDetailView.SubtaskStatsView(
                workItemReadPort.countActiveChildrenByParentId(query.projectId(), workItem.getId(), query.tenantId()),
                workItemReadPort.countDoneChildrenByParentId(query.projectId(), workItem.getId(), query.tenantId())
        );
        WorkItemDetailView.LinkStatsView linkStats = new WorkItemDetailView.LinkStatsView(
                workItemReadPort.countActiveLinksByWorkItemId(workItem.getId(), query.tenantId())
        );

        return WorkItemDetailView.from(
                workItem,
                userSummaryOrId(users, workItem.getAssigneeId()),
                userSummaryOrId(users, workItem.getReporterId()),
                components,
                subtaskStats,
                linkStats
        );
    }

    private Map<Long, WorkItemDetailView.UserSummaryView> resolveUserSummaries(WorkItemDetailProjection workItem) {
        List<Long> userIds = Stream.of(workItem.getAssigneeId(), workItem.getReporterId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            return userProfileClient.getUserProfilesByIds(userIds).stream()
                    .filter(Objects::nonNull)
                    .filter(profile -> profile.getId() != null)
                    .collect(Collectors.toMap(
                            UserProfileDto::getId,
                            this::toUserSummary,
                            (left, right) -> left
                    ));
        } catch (Exception e) {
            log.warn("[GetWorkItemByIdQueryHandler] Failed to resolve user profiles: {}", e.getMessage());
            return Map.of();
        }
    }

    private WorkItemDetailView.UserSummaryView toUserSummary(UserProfileDto profile) {
        String displayName = profile.getFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = profile.getEmail();
        }
        return new WorkItemDetailView.UserSummaryView(profile.getId(), displayName, profile.getAvatarUrl());
    }

    private static WorkItemDetailView.UserSummaryView userSummaryOrId(Map<Long, WorkItemDetailView.UserSummaryView> map,
                                                                      Long key) {
        if (key == null) {
            return null;
        }
        return map.getOrDefault(key, new WorkItemDetailView.UserSummaryView(key, null, null));
    }

}
