/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.activity;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.dto.WorkItemActivityProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemActivityView(
        String id,
        String type,
        UserSummaryView actor,
        String body,
        String fieldKey,
        String fieldName,
        String fromValue,
        String toValue,
        Long createdAt
) {
    public static WorkItemActivityView from(WorkItemActivityProjection projection, UserSummaryView actor) {
        return new WorkItemActivityView(
                projection.id(),
                projection.type(),
                actor,
                projection.body(),
                projection.fieldKey(),
                projection.fieldName(),
                projection.fromValue(),
                projection.toValue(),
                projection.createdAt()
        );
    }

    public record UserSummaryView(Long id, String displayName, String avatarUrl) {
    }
}
