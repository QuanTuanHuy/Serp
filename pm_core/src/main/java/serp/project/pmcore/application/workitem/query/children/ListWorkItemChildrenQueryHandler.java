/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.children;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.WorkItemChildProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListWorkItemChildrenQueryHandler implements IQueryHandler<ListWorkItemChildrenQuery, List<WorkItemChildView>> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService permissionEvaluationService;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemChildView> handle(ListWorkItemChildrenQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        permissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        workItemReadPort.getWorkItemDetailById(query.projectId(), query.workItemId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.workItem(query.workItemId()));

        List<WorkItemChildProjection> children = workItemReadPort.listChildrenByParentId(
                query.projectId(),
                query.workItemId(),
                query.tenantId()
        );
        Map<Long, UserSummary> users = resolveUserSummaries(children);
        return children.stream()
                .map(child -> WorkItemChildView.from(child, userSummaryOrId(users, child.assigneeId())))
                .toList();
    }

    private Map<Long, UserSummary> resolveUserSummaries(List<WorkItemChildProjection> children) {
        List<Long> userIds = children.stream()
                .map(WorkItemChildProjection::assigneeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            return userService.getUserProfilesByIds(userIds).stream()
                    .filter(Objects::nonNull)
                    .filter(profile -> profile.getId() != null)
                    .collect(Collectors.toMap(
                            UserProfileDto::getId,
                            UserSummary::from,
                            (left, right) -> left
                    ));
        } catch (Exception e) {
            log.warn("[ListWorkItemChildrenQueryHandler] Failed to resolve user profiles: {}", e.getMessage());
            return Map.of();
        }
    }

    private static UserSummary userSummaryOrId(Map<Long, UserSummary> map, Long key) {
        if (key == null) {
            return null;
        }
        return map.getOrDefault(key, UserSummary.missing(key));
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
