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
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.user.service.IUserService;
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
    private final IUserService userService;

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
        Map<Long, UserSummary> users = resolveUsers(page);
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

    private Map<Long, UserSummary> resolveUsers(Page<WorkItemCommentEntity> page) {
        var userIds = page.getContent().stream()
                .map(WorkItemCommentEntity::getAuthorId)
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
        } catch (Exception e) {
            log.warn("[ListWorkItemCommentsQueryHandler] Failed to resolve user profiles: {}", e.getMessage());
            return Map.of();
        }
    }

    private UserSummary userOrId(Map<Long, UserSummary> users, Long userId) {
        return userId == null ? null : users.getOrDefault(userId, UserSummary.missing(userId));
    }
}
