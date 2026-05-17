/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemCommentReadPort;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListWorkItemCommentsQueryHandler implements IQueryHandler<ListWorkItemCommentsQuery, PageView<WorkItemCommentView>> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IWorkItemCommentReadPort commentReadPort;
    private final IUserProfileClient userProfileClient;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkItemCommentView> handle(ListWorkItemCommentsQuery query) {
        accessHelper.requireReadableWorkItem(query.projectId(), query.workItemId(), query.tenantId(), query.userId(), query.groupKeys());
        PageRequest pageRequest = PageRequest.of(
                Math.max(query.page(), 0),
                Math.min(Math.max(query.size(), 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<WorkItemCommentEntity> page = commentReadPort.listByWorkItemId(query.workItemId(), query.tenantId(), pageRequest);
        Map<Long, WorkItemCommentView.UserSummaryView> users = resolveUsers(page);
        return new PageView<>(
                page.getContent().stream()
                        .map(comment -> WorkItemCommentView.from(comment, userOrId(users, comment.getAuthorId())))
                        .toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    private Map<Long, WorkItemCommentView.UserSummaryView> resolveUsers(Page<WorkItemCommentEntity> page) {
        var userIds = page.getContent().stream()
                .map(WorkItemCommentEntity::getAuthorId)
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
                    .collect(Collectors.toMap(UserProfileDto::getId, this::toUserSummary, (left, right) -> left));
        } catch (Exception e) {
            log.warn("[ListWorkItemCommentsQueryHandler] Failed to resolve user profiles: {}", e.getMessage());
            return Map.of();
        }
    }

    private WorkItemCommentView.UserSummaryView toUserSummary(UserProfileDto profile) {
        String displayName = profile.getFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = profile.getEmail();
        }
        return new WorkItemCommentView.UserSummaryView(profile.getId(), displayName, profile.getAvatarUrl());
    }

    private WorkItemCommentView.UserSummaryView userOrId(Map<Long, WorkItemCommentView.UserSummaryView> users, Long userId) {
        return userId == null ? null : users.getOrDefault(userId, new WorkItemCommentView.UserSummaryView(userId, null, null));
    }
}
