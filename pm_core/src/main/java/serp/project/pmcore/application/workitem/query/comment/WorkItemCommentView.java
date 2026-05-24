/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemCommentView(
        Long id,
        String body,
        UserSummary author,
        Long createdAt,
        Long updatedAt,
        boolean edited
) {
    public static WorkItemCommentView from(WorkItemCommentEntity comment, UserSummary author) {
        return new WorkItemCommentView(
                comment.getId(),
                comment.getBody(),
                author,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getUpdatedAt() != null && !comment.getUpdatedAt().equals(comment.getCreatedAt())
        );
    }
}

