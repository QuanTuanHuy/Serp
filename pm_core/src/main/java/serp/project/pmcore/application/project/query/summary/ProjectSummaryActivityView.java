/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectSummaryActivityView(
        String id,
        String type,
        UserSummary actor,
        Long workItemId,
        String workItemKey,
        String workItemSummary,
        Long statusId,
        String statusKey,
        String statusName,
        String body,
        String fieldKey,
        String fieldName,
        String fromValue,
        String toValue,
        Long createdAt
) {
    public static ProjectSummaryActivityView from(ProjectSummaryActivityProjection projection, UserSummary actor) {
        return new ProjectSummaryActivityView(
                projection.id(),
                projection.type(),
                actor,
                projection.workItemId(),
                projection.workItemKey(),
                projection.workItemSummary(),
                projection.statusId(),
                projection.statusKey(),
                projection.statusName(),
                projection.body(),
                projection.fieldKey(),
                projection.fieldName(),
                projection.fromValue(),
                projection.toValue(),
                projection.createdAt()
        );
    }
}
