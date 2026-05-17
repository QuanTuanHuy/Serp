/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemCommentView(
        Long id,
        String body,
        UserSummaryView author,
        Long createdAt,
        Long updatedAt,
        boolean edited
) {
    public static WorkItemCommentView from(WorkItemCommentEntity comment, UserSummaryView author) {
        return new WorkItemCommentView(
                comment.getId(),
                comment.getBody(),
                author,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getUpdatedAt() != null && !comment.getUpdatedAt().equals(comment.getCreatedAt())
        );
    }

    public record UserSummaryView(Long id, String displayName, String avatarUrl) {
    }
}
