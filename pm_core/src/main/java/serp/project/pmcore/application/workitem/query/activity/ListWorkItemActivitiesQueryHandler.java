/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.WorkItemActivityProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListWorkItemActivitiesQueryHandler implements IQueryHandler<ListWorkItemActivitiesQuery, PageView<WorkItemActivityView>> {

    private static final Set<String> SUPPORTED_TYPES = Set.of("ALL", "COMMENT", "HISTORY");

    private final WorkItemComponentAccessHelper accessHelper;
    private final IWorkItemReadPort workItemReadPort;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkItemActivityView> handle(ListWorkItemActivitiesQuery query) {
        accessHelper.requireReadableWorkItem(query.projectId(), query.workItemId(), query.tenantId(), query.userId(), query.groupKeys());
        String type = normalizeType(query.type());
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        PageResult<WorkItemActivityProjection> result = workItemReadPort.listWorkItemActivities(
                query.workItemId(),
                query.tenantId(),
                type,
                page,
                size
        );
        Map<Long, UserSummary> users = resolveUsers(result);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) result.total() / size);
        return new PageView<>(
                result.items().stream()
                        .map(activity -> WorkItemActivityView.from(activity, userOrId(users, activity.actorId())))
                        .toList(),
                result.total(),
                totalPages,
                page,
                size
        );
    }

    private String normalizeType(String type) {
        String normalized = type == null || type.isBlank() ? "ALL" : type.trim().toUpperCase();
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new DomainValidationException(DomainErrorCode.BAD_REQUEST, "Unsupported activity type: " + type);
        }
        return normalized;
    }

    private Map<Long, UserSummary> resolveUsers(PageResult<WorkItemActivityProjection> result) {
        var userIds = result.items().stream()
                .map(WorkItemActivityProjection::actorId)
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
                    .collect(Collectors.toMap(UserProfileDto::getId, UserSummary::from, (left, right) -> left));
        } catch (Exception ex) {
            log.warn("[ListWorkItemActivitiesQueryHandler] Failed to resolve user profiles: {}", ex.getMessage());
            return Map.of();
        }
    }

    private UserSummary userOrId(Map<Long, UserSummary> users, Long userId) {
        return userId == null ? null : users.getOrDefault(userId, UserSummary.missing(userId));
    }
}
