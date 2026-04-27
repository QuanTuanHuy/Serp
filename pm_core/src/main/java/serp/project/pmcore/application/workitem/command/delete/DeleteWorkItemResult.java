/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import lombok.Builder;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;

@Builder
public record DeleteWorkItemResult(
        Long rootWorkItemId,
        Integer deletedWorkItemCount,
        Integer deletedRelationCount,
        Integer deletedLinkCount,
        Long deletedAt
) {
    public DeleteWorkItemResult {
        if (deletedAt == null) {
            deletedAt = System.currentTimeMillis();
        }
    }

    public static DeleteWorkItemResult from(Long rootWorkItemId,
                                            WorkItemDeleteExecutionResult result,
                                            Long deletedAt) {
        return new DeleteWorkItemResult(
                rootWorkItemId,
                result.deletedWorkItemCount(),
                result.deletedRelationCount(),
                result.deletedLinkCount(),
                deletedAt
        );
    }
}
