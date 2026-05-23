/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListWorkItemBoardQueryHandler implements IQueryHandler<ListWorkItemBoardQuery, WorkItemBoardView> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public WorkItemBoardView handle(ListWorkItemBoardQuery query) {
        ProjectEntity project = projectService.getProjectById(query.criteria().getProjectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        List<WorkItemBoardStatusProjection> statuses = workItemReadPort.listBoardStatuses(query.tenantId(), query.criteria());
        List<WorkItemBoardItemProjection> boardItems = workItemReadPort.listBoardWorkItems(query.tenantId(), query.criteria());
        Map<Long, UserSummary> assigneeSummaryMap = resolveAssigneeSummaries(boardItems);
        Map<Long, List<WorkItemBoardCardView>> itemsByStatusId = boardItems.stream()
                .collect(Collectors.groupingBy(
                        WorkItemBoardItemProjection::statusId,
                        Collectors.mapping(
                                item -> WorkItemBoardCardView.from(
                                        applyAssigneeSummary(item, assigneeSummaryMap.get(item.assigneeId()))
                                ),
                                Collectors.toList()
                        )
                ));

        List<WorkItemBoardColumnView> columns = statuses.stream()
                .map(status -> WorkItemBoardColumnView.from(
                        status,
                        itemsByStatusId.getOrDefault(status.statusId(), List.of())
                ))
                .toList();

        return new WorkItemBoardView(query.criteria().getProjectId(), columns);
    }

    private Map<Long, UserSummary> resolveAssigneeSummaries(List<WorkItemBoardItemProjection> items) {
        List<Long> userIds = items.stream()
                .map(WorkItemBoardItemProjection::assigneeId)
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

    private WorkItemBoardItemProjection applyAssigneeSummary(WorkItemBoardItemProjection item, UserSummary assignee) {
        return new WorkItemBoardItemProjection(
                item.id(),
                item.projectId(),
                item.parentId(),
                item.key(),
                item.summary(),
                item.description(),
                item.assigneeId(),
                assignee != null ? assignee.displayName() : item.assigneeName(),
                assignee != null ? assignee.avatarUrl() : item.assigneeAvatarUrl(),
                item.reporterId(),
                item.startDate(),
                item.dueDate(),
                item.rank(),
                item.issueTypeId(),
                item.issueTypeName(),
                item.issueTypeIconUrl(),
                item.issueTypeHierarchyLevel(),
                item.statusId(),
                item.statusKey(),
                item.statusName(),
                item.priorityId(),
                item.priorityName(),
                item.priorityIconUrl(),
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
