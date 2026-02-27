/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemResponse {
    private Long id;
    private Long projectId;
    private Long issueTypeId;

    private Long issueNo;
    private String key;
    private String summary;
    private String description;

    private Long statusId;
    private Long priorityId;
    private Long resolutionId;
    private Long assigneeId;
    private Long reporterId;
    private Long parentId;

    private Long dueDate;
    private String rank;

    private Long timeOriginalEstimate;
    private Long timeRemainingEstimate;
    private Long timeSpent;

    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;
}
